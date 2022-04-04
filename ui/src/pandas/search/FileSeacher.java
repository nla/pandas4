package pandas.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.jetbrains.annotations.NotNull;
import org.netpreserve.jwarc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pandas.util.Strings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class FileSeacher {
    private static final List<Filter> FILTERS = List.of(
            new Filter("Status", "status", FileSeacher::labelForStatus),
            new Filter("Type", "type"),
            new Filter("Host", "host"));
    private static final Set<String> facetFields = FILTERS.stream().map(Filter::field).collect(Collectors.toUnmodifiableSet());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(FileSeacher.class);
    private final Directory directory;
    private final Analyzer analyzer = new StandardAnalyzer();
    private final FacetsConfig facetsConfig = new FacetsConfig();
    private final File stateFile;

    public FileSeacher(Path index) throws IOException {
        this.directory = new MMapDirectory(index);
        stateFile = index.resolve("state.json").toFile();
    }

    private static boolean isWarcFile(Path path) {
        String filename = path.getFileName().toString();
        return filename.endsWith(".warc.gz") || filename.endsWith(".warc.gz.open");
    }

    public void indexRecursively(Path warcDirectory) throws IOException {
        long start = System.nanoTime();
        var documentsAdded = new AtomicLong();
        try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND))) {
            var indexingState = loadState();
            var filesToIndex = scanForUpdatedWarcFiles(warcDirectory, indexingState);
            try {
                filesToIndex.parallelStream()
                        .forEach(path -> documentsAdded.addAndGet(indexWarc(indexWriter, warcDirectory, path, indexingState)));
                log.info("{}ms elapsed adding {} records from {} WARCs in {}", (System.nanoTime() - start) / 1000000, documentsAdded, filesToIndex.size(), warcDirectory);

            } finally {
                saveState(indexingState);
            }
        }
    }

    @NotNull
    private ArrayList<Path> scanForUpdatedWarcFiles(Path warcDirectory, Map<String, IndexingState> indexingState) throws IOException {
        var allWarcFiles = Files.walk(warcDirectory)
                .filter(FileSeacher::isWarcFile)
                .toList();
        var warcFilesToIndex = new ArrayList<Path>();
        for (Path warcPath : allWarcFiles) {
            long size = Files.size(warcPath);
            String key = warcDirectory.relativize(warcPath).toString();
            IndexingState fileState = indexingState.computeIfAbsent(key, k -> new IndexingState());
            if (size > fileState.size) {
                fileState.size = size;
                indexingState.put(key, fileState);
                warcFilesToIndex.add(warcPath);
            }
        }
        return warcFilesToIndex;
    }

    private void saveState(Map<String, IndexingState> indexingState) throws IOException {
        objectMapper.writeValue(stateFile, indexingState);
    }

    private ConcurrentHashMap<String, IndexingState> loadState() throws IOException {
        try {
            return objectMapper.readValue(stateFile,
                    new TypeReference<>() {
                    });
        } catch (FileNotFoundException e) {
            return new ConcurrentHashMap<>();
        }
    }

    private static class ConcurrentSet {
        private final HashSet<URI> set = new HashSet<>();

        boolean contains(WarcRecord record) {
            if (set.contains(record.id())) {
                return true;
            } else if (record instanceof WarcCaptureRecord capture) {
                for (var id : capture.concurrentTo()) {
                    return true;
                }
            }
            return false;
        }

        boolean checkAndAdd(WarcRecord record) {
            boolean isConcurrent = contains(record);
            if (!isConcurrent) {
                set.clear();
            }
            set.add(record.id());
            if (record instanceof WarcCaptureRecord capture) {
                set.addAll(capture.concurrentTo());
            }
            return isConcurrent;
        }
    }

    private long indexWarc(IndexWriter indexWriter, Path warcDirectory, Path path, ConcurrentHashMap<String, IndexingState> indexingState) {
        log.info("Indexing {}", path);
        IndexingState fileState = indexingState.get(warcDirectory.relativize(path).toString());
        long documentsAdded = 0;
        try (var channel = FileChannel.open(path)) {
            channel.position(fileState.position);
            var warcReader = new WarcReader(channel);
            var concurrentSet = new ConcurrentSet();
            var doc = new Document();
            boolean seenResource = false;
            for (WarcRecord record : warcReader) {
                if (!concurrentSet.checkAndAdd(record)) {
                    if (seenResource) {
                        indexWriter.addDocument(facetsConfig.build(doc));
                        documentsAdded++;
                    }
                    doc.clear();
                    seenResource = false;
                }

                if (record instanceof WarcMetadata) {
                    doc.add(new StoredField("metadataOffset", warcReader.position()));
                } else if (record instanceof WarcRequest request && request.contentType().equals(MediaType.HTTP_REQUEST)) {
                    doc.add(new StringField("method", request.http().method(), Field.Store.YES));
                    doc.add(new StoredField("requestOffset", warcReader.position()));
                } else if (record instanceof WarcResponse response && response.contentType().equals(MediaType.HTTP_RESPONSE)) {
                    seenResource = true;
                    var host = response.targetURI().getHost();
                    doc.add(new StringField("responseId", Strings.removePrefix("urn:uuid:", response.id().toString()), Field.Store.YES));
                    doc.add(new StoredField("responseOffset", warcReader.position()));
                    doc.add(new StoredField("warcFile", warcDirectory.relativize(path).toString()));
                    doc.add(new LongPoint("date", response.date().toEpochMilli()));
                    doc.add(new SortedNumericDocValuesField("date", response.date().toEpochMilli()));
                    doc.add(new StoredField("date", response.date().toEpochMilli()));
                    doc.add(new TextField("url", response.target(), Field.Store.YES));
                    String type;
                    try {
                        type = response.http().contentType().subtype();
                    } catch (IllegalArgumentException e) {
                        type = "octet-stream";
                    }
                    doc.add(new StringField("status", String.valueOf(response.http().status()), Field.Store.YES));
                    doc.add(new SortedSetDocValuesFacetField("facet_status", String.valueOf(response.http().status())));
                    doc.add(new StoredField("size", response.http().body().size()));
                    doc.add(new NumericDocValuesField("size", response.http().body().size()));
                    doc.add(new StringField("type", type, Field.Store.YES));
                    doc.add(new SortedSetDocValuesFacetField("facet_type", type));
                    doc.add(new TextField("host", host, Field.Store.YES));
                    doc.add(new SortedSetDocValuesFacetField("facet_host", host));
                    response.payloadDigest().ifPresent(digest ->
                            doc.add(new TextField(digest.algorithm(), digest.base32(), Field.Store.YES)));
                    doc.add(new StoredField("position", warcReader.position()));
                }
                fileState.position = warcReader.position();
            }
            if (seenResource) {
                indexWriter.addDocument(facetsConfig.build(doc));
                documentsAdded++;
            }

            return documentsAdded;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Result> searchById(String id) throws IOException {
        try (var indexReader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(indexReader);
            var topDocs = searcher.search(new TermQuery(new Term("responseId", id)), 1);
            if (topDocs.scoreDocs.length == 0) return Optional.empty();
            return Optional.of(new Result(searcher.doc(topDocs.scoreDocs[0].doc)));
        }
    }

    public Results search(String q, MultiValueMap<String, String> filters, Pageable pageable) throws IOException {
        try (var indexReader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(indexReader);
            var state = new DefaultSortedSetDocValuesReaderState(indexReader);
            BooleanQuery query = buildQuery(q, filters, null);

            var facetsCollector = new FacetsCollector();

            var sizeStats = new DocValuesStats.LongDocValuesStats("size");
            var sizeStatsCollector = new DocValuesStatsCollector(sizeStats);
            int endOffset = (int) (pageable.getOffset() + pageable.getPageSize());

            var sortFields = new ArrayList<SortField>();
            for (var order : pageable.getSort()) {
                switch (order.getProperty()) {
                    case "date", "size" -> sortFields.add(new SortedNumericSortField(order.getProperty(),
                            SortField.Type.LONG, order.isDescending()));
                    default -> {
                    }
                }
            }
            Collector multiCollector = MultiCollector.wrap(sizeStatsCollector, facetsCollector);
            TopDocs topDocs;
            if (sortFields.isEmpty()) {
                topDocs = FacetsCollector.search(searcher, query, endOffset, multiCollector);
            } else {
                topDocs = FacetsCollector.search(searcher, query,
                        endOffset, new Sort(sortFields.toArray(new SortField[0])),
                        multiCollector);
            }
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
            var resultList = new ArrayList<Result>(pageable.getPageSize());
            for (int i = (int) pageable.getOffset(); i < endOffset && i < topDocs.scoreDocs.length; i++) {
                Document doc = searcher.doc(topDocs.scoreDocs[i].doc);
                if (doc != null) {
                    resultList.add(new Result(doc));
                }
            }

            return new Results(resultList, pageable, topDocs.totalHits.value, facetResults, sizeStats.sum());
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
        return queryBuilder.build();
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

    public static final class Results extends PageImpl<Result> {
        private final List<FacetResults> facets;
        private final Long totalBytes;

        public Results(List<Result> list, Pageable pageable, long totalHits,
                       List<FacetResults> facets, Long totalBytes) {
            super(list, pageable, totalHits);
            this.facets = facets;
            this.totalBytes = totalBytes;
        }

        public List<FacetResults> facets() {
            return facets;
        }

        public Long totalBytes() {
            return totalBytes;
        }
    }

    public record Result(String id, Instant date, String method, String url, Integer status, Long size, String type,
                         String sha1, Long requestOffset, Long responseOffset, Long metadataOffset, Path warcFile) {
        Result(Document doc) {
            this(doc.get("responseId"),
                    Instant.ofEpochMilli((Long) doc.getField("date").numericValue()),
                    doc.get("method"),
                    doc.get("url"),
                    Integer.parseInt(doc.get("status")),
                    (Long) doc.getField("size").numericValue(),
                    doc.get("type"),
                    doc.get("sha1"),
                    getLongField(doc, "requestOffset"),
                    getLongField(doc, "responseOffset"),
                    getLongField(doc, "metadataOffset"),
                    Paths.get(doc.get("warcFile")));
        }
    }

    private static Long getLongField(Document doc, String name) {
        var field = doc.getField(name);
        if (field == null) return null;
        return (Long) field.numericValue();
    }

    public static class IndexingState {
        public long size;
        public long position;

        public IndexingState() {
        }

        public IndexingState(long size) {
            this.size = size;
        }
    }

    public static void main(String[] args) throws IOException {
        Path indexPath = Paths.get(args[0]);
        FileSystemUtils.deleteRecursively(indexPath);
        FileSeacher index = new FileSeacher(indexPath);
        index.indexRecursively(Paths.get(args[1]));
        index.search("", new LinkedMultiValueMap<>(), Pageable.ofSize(100));
    }
}
