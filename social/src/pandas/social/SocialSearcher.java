package pandas.social;

import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.QueryBuilder;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static pandas.social.SocialIndexFields.*;

@Service
public class SocialSearcher implements Closeable {
    private final SearcherManager searcherManager;
    private static final Pattern TOKENISER = Pattern.compile("\"([^\"]*)\"|\\S+");

    private static final Map<String, Sort> SORTS = Map.of(
            "newest", new Sort(new SortedNumericSortField(CREATED_AT, SortField.Type.LONG, true)),
            "oldest", new Sort(new SortedNumericSortField(CREATED_AT, SortField.Type.LONG, false)),
            "relevance", new Sort(SortField.FIELD_SCORE));

    public SocialSearcher(SocialIndexer indexer) throws IOException {
        searcherManager = new SearcherManager(indexer.indexWriter, null);
    }

    private static Query parseQuery(String queryString) {
        var query = new BooleanQuery.Builder();
        QueryBuilder queryBuilder = new QueryBuilder(new EnglishAnalyzer());
        var textTokens = new ArrayList<String>();

        if (queryString.isBlank()) {
            return query.add(new MatchAllDocsQuery(), MUST).build();
        }

        var matcher = TOKENISER.matcher(queryString);
        while (matcher.find()) {
            if (matcher.start(1) != -1) {
                query.add(queryBuilder.createPhraseQuery(TEXT, matcher.group(1)), MUST);
            } else {
                String token = matcher.group();
                if (token.startsWith("@")) {
                    query.add(new TermQuery(new Term(MENTIONS, token.substring("@".length()).toLowerCase(Locale.ROOT))), MUST);
                } else if (token.startsWith("from:")) {
                    query.add(new TermQuery(new Term(FROM, token.substring("from:".length()).toLowerCase(Locale.ROOT))), MUST);
                } else if (token.startsWith("mentions:")) {
                    query.add(new TermQuery(new Term(MENTIONS, token.substring("mentions:".length()).toLowerCase(Locale.ROOT))), MUST);
                } else if (token.startsWith("to:")) {
                    query.add(new TermQuery(new Term(TO, token.substring("to:".length()).toLowerCase(Locale.ROOT))), MUST);
                } else {
                    textTokens.add(token);
                }
            }
        }
        if (!textTokens.isEmpty()) {
            query.add(queryBuilder.createBooleanQuery(TEXT, String.join(" ", textTokens)), MUST);
        }
        return query.build();
    }

    public SocialResults search(String query, String sortString) throws IOException {
        searcherManager.maybeRefresh();
        IndexSearcher indexSearcher = searcherManager.acquire();
        try {
            Sort sort = SORTS.get(sortString);
            if (sort == null) throw new IllegalArgumentException("Unknown sort: " + sortString);
            var topHits = indexSearcher.search(parseQuery(query), 40, sort);
            var fieldsToLoad = Set.of(JSON);
            var posts = new ArrayList<Post>(topHits.scoreDocs.length);
            for (int i = 0; i < topHits.scoreDocs.length; i++) {
                var doc = indexSearcher.doc(topHits.scoreDocs[i].doc, fieldsToLoad);
                posts.add(SocialJson.mapper.readValue(doc.get(JSON), Post.class));
            }
            return new SocialResults(topHits.totalHits.value, posts);
        } finally {
            searcherManager.release(indexSearcher);
        }
    }


    public PostDetails findByUrl(String url) throws IOException {
        IndexSearcher indexSearcher = searcherManager.acquire();
        try {
            System.err.println("findByUrl: " + url);
            var topHits = indexSearcher.search(new TermQuery(new Term(URL, url)), 1);
            if (topHits.scoreDocs.length == 0) return null;
            var doc = indexSearcher.doc(topHits.scoreDocs[0].doc,
                    Set.of(JSON, WARC_FILENAME, WARC_OFFSET, WARC_DATE_STORED));
            Post post = SocialJson.mapper.readValue(doc.get(JSON), Post.class);
            return new PostDetails(post, doc.get(WARC_FILENAME), doc.getField(WARC_OFFSET).numericValue().longValue(),
                    Instant.ofEpochMilli(doc.getField(WARC_DATE_STORED).numericValue().longValue()));
        } finally {
            searcherManager.release(indexSearcher);
        }
    }

    public record PostDetails(Post post, String warcFile, long warcOffset, Instant warcDate) {
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        searcherManager.close();
    }
}
