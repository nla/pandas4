package pandas.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.jetbrains.annotations.NotNull;
import org.netpreserve.jwarc.MediaType;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class FileSeacher {
    private static final List<Filter> FILTERS = List.of(
            new Filter("Status Code", "status", FileSeacher::labelForStatus),
            new Filter("Content Type", "mime"),
            new Filter("Host", "host"));
    private static final Set<String> facetFields = FILTERS.stream().map(Filter::field).collect(Collectors.toUnmodifiableSet());

    private final Logger log = LoggerFactory.getLogger(FileSeacher.class);
    private final Directory directory;
    private final Analyzer analyzer = new StandardAnalyzer();
    private final FacetsConfig facetsConfig = new FacetsConfig();

    public FileSeacher(Path index) throws IOException {
        this.directory = new MMapDirectory(index);
    }

    public void indexRecursively(Path warcDirectory) throws IOException {
        long start = System.nanoTime();
        var documentsAdded = new AtomicLong();
        try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND))) {
            List<Path> warcs = Files.walk(warcDirectory)
                    .filter(path -> path.getFileName().toString().endsWith(".warc.gz"))
                    .toList();
            warcs.parallelStream().forEach(path -> documentsAdded.addAndGet(indexWarc(indexWriter, path)));
            log.info("{}ms elapsed adding {} records from {} WARCs in {}", (System.nanoTime() - start) / 1000000, documentsAdded, warcs.size(), warcDirectory);
        }
    }

    private long indexWarc(IndexWriter indexWriter, Path path) {
        log.info("Indexing {}", path);
        long documentsAdded = 0;
        try (var warcReader = new WarcReader(path)) {
            for (var record : warcReader) {
                if (record instanceof WarcResponse response && response.contentType().equals(MediaType.HTTP_RESPONSE)) {
                    var host = response.targetURI().getHost();
                    var doc = new Document();
                    doc.add(new LongPoint("date", response.date().toEpochMilli()));
                    doc.add(new StoredField("date", response.date().toEpochMilli()));
                    doc.add(new TextField("url", response.target(), Field.Store.YES));
                    String mime;
                    try {
                        mime = response.http().contentType().base().toString();
                    } catch (IllegalArgumentException e) {
                        mime = "application/octet-stream";
                    }
                    doc.add(new StringField("status", String.valueOf(response.http().status()), Field.Store.YES));
                    doc.add(new SortedSetDocValuesFacetField("facet_status", String.valueOf(response.http().status())));
                    doc.add(new StoredField("size", response.http().body().size()));
                    doc.add(new NumericDocValuesField("size", response.http().body().size()));
                    doc.add(new StringField("mime", mime, Field.Store.YES));
                    doc.add(new SortedSetDocValuesFacetField("facet_mime", mime));
                    doc.add(new TextField("host", host, Field.Store.YES));
                    doc.add(new SortedSetDocValuesFacetField("facet_host", host));
                    response.payloadDigest().ifPresent(digest ->
                            doc.add(new TextField(digest.algorithm(), digest.base32(), Field.Store.YES)));
                    doc.add(new StoredField("position", warcReader.position()));
                    indexWriter.addDocument(facetsConfig.build(doc));
                    documentsAdded++;
                }
            }
            return documentsAdded;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Results search(String q, MultiValueMap<String, String> filters) throws IOException {
        try (var indexReader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(indexReader);
            var state = new DefaultSortedSetDocValuesReaderState(indexReader);
            BooleanQuery query = buildQuery(q, filters, null);

            var facetsCollector = new FacetsCollector();

            var sizeStats = new DocValuesStats.LongDocValuesStats("size");
            var sizeStatsCollector = new DocValuesStatsCollector(sizeStats);
            var topDocs = FacetsCollector.search(searcher, query, 100,
                    MultiCollector.wrap(sizeStatsCollector, facetsCollector));
            var facetCounts = new SortedSetDocValuesFacetCounts(state, facetsCollector);
            var facetResults = new ArrayList<FacetResults>();

            System.out.println(topDocs.totalHits.value + " hits for " + query);

            // Calculate the facet counts for each set of filters
            for (Filter filter : FILTERS) {
                Facets counts;
                if (filters.containsKey(filter.field)) {
                    // filters that have been applied need their own search which excludes the filtered terms
                    // as the main search would only have results for the selected options
                    var collector = new FacetsCollector();
                    FacetsCollector.search(searcher, buildQuery(q, filters, filter.field), 1, collector);
                    counts = new SortedSetDocValuesFacetCounts(state, collector);
                } else {
                    // filters that haven't been selected can just use the facets from the main search
                    counts = facetCounts;
                }

                FacetResult result = counts.getTopChildren(10, "facet_" + filter.field);
                if (result == null) continue;
                var entries = Arrays.stream(result.labelValues)
                        .map(lv -> new FacetEntry(lv.label, filter.labelFunction.apply(lv.label), lv.value.longValue(),
                                filters.getOrDefault(filter.field, emptyList()).contains(lv.label))).toList();
                facetResults.add(new FacetResults(filter.name, filter.field, entries, true, false, null));
            }

            // Construct a result for each matching document
            var resultList = new ArrayList<Result>(topDocs.scoreDocs.length);
            for (var scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                if (doc != null) {
                    resultList.add(new Result(
                            Instant.ofEpochMilli((Long) doc.getField("date").numericValue()),
                            doc.get("url"),
                            Integer.parseInt(doc.get("status")),
                            (Long) doc.getField("size").numericValue(),
                            doc.get("mime"),
                            doc.get("sha1")));
                }
            }

            return new Results(topDocs.totalHits.value, resultList, facetResults, sizeStats.sum());
        }
    }

    private BooleanQuery buildQuery(String q, MultiValueMap<String, String> filters, String excludedFilter) {
        var parser = new SimpleQueryParser(analyzer, "url");
        var queryBuilder = new BooleanQuery.Builder();
        for (var entry : filters.entrySet()) {
            if (!facetFields.contains(entry.getKey())) continue;
            if (entry.getKey().equals(excludedFilter)) continue;

            if (entry.getValue().size() > 1) {
                var innerBuilder = new BooleanQuery.Builder();
                for (var value : entry.getValue()) {
                    innerBuilder.add(new TermQuery(new Term(entry.getKey(), value)), Occur.SHOULD);
                }
                queryBuilder.add(innerBuilder.build(), Occur.MUST);
            } else {
                queryBuilder.add(new TermQuery(new Term(entry.getKey(), entry.getValue().get(0))), Occur.MUST);
            }
        }
        queryBuilder.add(q.isBlank() ? new MatchAllDocsQuery() : parser.parse(q), Occur.MUST);
        var query = queryBuilder.build();
        return query;
    }

    record Filter(String name, String field, Function<String, String> labelFunction) {
        Filter(String name, String field) {
            this(name, field, Function.identity());
        }
    }

    private static String labelForStatus(String code) {
        var reason = HttpStatus.resolve(Integer.parseInt(code));
        return reason == null ? code : code + " " + reason.getReasonPhrase();
    }

    public record Results(long totalHits, List<Result> list,
                          List<FacetResults> facets, Long totalSize) implements Iterable<Result> {
        @NotNull
        @Override
        public Iterator<Result> iterator() {
            return list.iterator();
        }
    }

    record Result(Instant date, String url, Integer status, Long size, String mime, String sha1) {
    }

    public static void main(String[] args) throws IOException {
        Path indexPath = Paths.get(args[0]);
        FileSystemUtils.deleteRecursively(indexPath);
        FileSeacher index = new FileSeacher(indexPath);
        index.indexRecursively(Paths.get(args[1]));
        index.search("", new LinkedMultiValueMap<>());
    }
}
