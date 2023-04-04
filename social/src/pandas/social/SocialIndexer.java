package pandas.social;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.MMapDirectory;
import org.jsoup.Jsoup;
import org.netpreserve.jwarc.WarcReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pandas.PandasSocialConfig;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

import static pandas.social.SocialIndexFields.*;

@Service
public class SocialIndexer implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(SocialIndexer.class);
    final IndexWriter indexWriter;

    public SocialIndexer(PandasSocialConfig config) throws IOException {
        this.indexWriter = new IndexWriter(new MMapDirectory(config.getIndexDir()), new IndexWriterConfig(new EnglishAnalyzer()));
    }

    public void addWarc(Path warcFile) throws IOException {
        try (var warcReader = new WarcReader(warcFile)) {
            addWarc(warcReader, warcFile.getFileName().toString());
        }
    }

    public long addWarc(WarcReader warcReader, String warcFilename) throws IOException {
        long postCount = 0;
        SocialReader socialReader = new SocialReader(warcReader);
        for (var posts = socialReader.nextBatch(); posts != null; posts = socialReader.nextBatch()) {
            var warcResponse = socialReader.warcResponse();
            for (var post : posts) {
                log.info("Adding {}:{} {}", warcFilename, warcReader.position(), post.url());
                addPost(post, warcFilename, warcReader.position(), warcResponse.date());
                postCount++;
            }
        }
        indexWriter.commit();
        return postCount;
    }

    public void addPost(Post post,
                        String warcFilename,
                        long position,
                        Instant warcDate) throws IOException {
        var html = Jsoup.parse(post.content());
        var doc = new Document();
        doc.add(new SortedNumericDocValuesField(CREATED_AT, post.createdAt().toEpochMilli()));
        doc.add(new StringField(URL, post.url(), Field.Store.YES));
        doc.add(new StringField(FROM, post.author().username().toLowerCase(Locale.ROOT), Field.Store.NO));
        for (var username: post.mentions()) {
            doc.add(new StringField(MENTIONS, username.toLowerCase(Locale.ROOT), Field.Store.NO));
        }
        if (post.to() != null) {
            doc.add(new StringField(FROM, post.to().toLowerCase(Locale.ROOT), Field.Store.NO));
            doc.add(new StringField(MENTIONS, post.to().toLowerCase(Locale.ROOT), Field.Store.NO));
        }

        addSortedNumericField(doc, REPLY_COUNT, post.replyCount());
        addSortedNumericField(doc, LIKE_COUNT, post.likeCount());
        addSortedNumericField(doc, REPOST_COUNT, post.repostCount());
        addSortedNumericField(doc, QUOTE_COUNT, post.quoteCount());

        doc.add(new TextField(TEXT, html.text(), Field.Store.NO));
        doc.add(new StoredField(JSON, SocialJson.mapper.writeValueAsString(post)));
        doc.add(new StringField(WARC_FILENAME, warcFilename, Field.Store.YES));
        doc.add(new StoredField(WARC_OFFSET, position));
        doc.add(new SortedNumericDocValuesField(WARC_DATE, warcDate.toEpochMilli()));
        doc.add(new StoredField(WARC_DATE_STORED, warcDate.toEpochMilli()));
        log.info(doc.getFields().toString());
        indexWriter.updateDocument(new Term(URL, post.url()), doc);
    }

    public void addSortedNumericField(Document doc, String field, Long value) {
        if (value != null) {
            doc.add(new SortedNumericDocValuesField(field, value));
        }
    }

    @Override
    public void close() throws IOException {
        indexWriter.close();
    }
}
