package pandas.social;

import dev.failsafe.*;
import net.openhft.hashing.LongHashFunction;
import org.jetbrains.annotations.NotNull;
import org.netpreserve.jwarc.*;
import org.netpreserve.jwarc.cdx.CdxReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;
import static org.netpreserve.jwarc.MessageVersion.HTTP_1_0;

public class AttachmentArchiver {
    private final static Logger log = LoggerFactory.getLogger(AttachmentArchiver.class);
    private final String cdxServerUrl;
    private final boolean dryRun;
    private final String userAgent;
    private final WarcWriterSupplier warcWriterSupplier;

    // Map from 64-bit URL hash to epoch millis.
    // Assumption: We aren't fetching millions of attachments in a single run so the probability of hash collision
    // is low enough to ignore.
    private final Map<Long, Long> seenUrlHashes = new HashMap<>();

    private final static FailsafeExecutor<FetchResult> failsafe = Failsafe.with(
            RetryPolicy.<FetchResult>builder()
                    .handleResultIf(result -> result.response().http().status() > 400 &&
                            result.response().http().status() != 404)
                    .handle(IOException.class)
                    .withBackoff(10, 10 * 60, ChronoUnit.SECONDS, 4)
                    .onRetry(e -> log.warn("Failure #{}. Retrying.", e.getAttemptCount(), e.getLastException()))
                    .build(),
            RateLimiter.<FetchResult>smoothBuilder(100, Duration.ofMinutes(5))
                    .withMaxWaitTime(Duration.ofMinutes(5))
                    .build(),
            CircuitBreaker.<FetchResult>builder()
                    .handleResultIf(result -> result.response().http().status() > 400 &&
                            result.response().http().status() != 404)
                    .handle(IOException.class)
                    .onOpen(e -> log.warn("Circuit breaker open"))
                    .onClose(e -> log.warn("Circuit breaker closed"))
                    .onHalfOpen(e -> log.warn("Circuit breaker half-opened"))
                    .withFailureThreshold(8)
                    .withSuccessThreshold(1)
                    .withDelay(Duration.ofMinutes(30))
                    .build());

    public AttachmentArchiver(WarcWriterSupplier warcWriterSupplier, String cdxServerUrl, boolean dryRun, String userAgent) {
        this.warcWriterSupplier = warcWriterSupplier;
        this.cdxServerUrl = cdxServerUrl;
        this.dryRun = dryRun;
        this.userAgent = userAgent;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        String cdxServerUrl = null;
        boolean dryRun = false;
        String userAgent = "pandas-social";
        List<URI> warcUris = new ArrayList<>();
        Path outputFile = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                switch (args[i]) {
                    case "-A", "--user-agent" -> userAgent = args[++i];
                    case "--cdx-server" -> cdxServerUrl = args[++i];
                    case "-h", "--help" -> {
                        System.out.println("""
                                Usage: AttachmentArchiver [options] [warc-file-or-url...]
                                                                
                                Options:
                                  -A, --user-agent   User-Agent header to make requests with
                                  --cdx-server URL   CDX server URL for deduplication
                                  -h, --help         Show this help message and exit
                                  -n, --dry-run      Just print URLs, don't actually fetch anything
                                  -o, --output FILE  Write output to FILE instead of stdout
                                """);
                        System.exit(0);
                    }
                    case "-n", "--dry-run" -> dryRun = true;
                    case "-o", "--output" -> outputFile = Path.of(args[++i]);
                    default -> {
                        System.err.println("Unknown option: " + args[i]);
                        System.exit(1);
                    }
                }
            } else {
                warcUris.add(args[i].startsWith("http://") || args[i].startsWith("https://") ?
                        new URI(args[i]) : Path.of(args[i]).toUri());
            }

            try (var warcWriter = outputFile == null ? new WarcWriter(System.out) :
                    new WarcWriter(FileChannel.open(outputFile, WRITE, CREATE, APPEND), WarcCompression.GZIP)) {
                var archiver = new AttachmentArchiver(() -> warcWriter, cdxServerUrl, dryRun, userAgent);
                for (var warcUri : warcUris) {
                    archiver.processWarc(warcUri);
                }
            }
        }
    }

    private void processWarc(URI warcUri) throws IOException {
        try (SocialReader socialReader = new SocialReader(warcUri)) {
            processWarc(socialReader);
        }
    }

    public void processWarc(SocialReader socialReader) throws IOException {
        for (var batch = socialReader.nextBatch(); batch != null; batch = socialReader.nextBatch()) {
            for (var post : batch) {
                visit(post, "");
            }
        }
    }

    void visit(Post post, String prefix) throws IOException {
        if (post == null) return;

        fetch("avatar", post.author().avatarUrl(), null, post.url());
        fetch("banner", post.author().bannerUrl(), null, post.url());

        for (var attachment : post.attachments()) {
            for (var source : attachment.sources()) {
                fetch("source", source.url(), source.contentType(), post.url());
            }
        }

        visit(post.quotedPost(), prefix + "  QP ");
        visit(post.repost(), prefix + "  RP ");
    }

    private void fetch(String type, String url, String contentType, String via) throws IOException {
        if (url == null || url.isBlank()) return;
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            log.warn("Skipping invalid URL \"{}\" via {}", url, via);
            return;
        }
        long urlHash = LongHashFunction.xx3().hashChars(url);

        Long cachedVisitMillis = seenUrlHashes.get(urlHash);
        if (cachedVisitMillis != null) {
            log.debug("Cache already contains {} at {}", url, Instant.ofEpochMilli(cachedVisitMillis));
            return;
        }

        if (cdxServerUrl != null) {
            var cdxQueryUrl = cdxServerUrl + "?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8) + "&sort=reverse&limit=20";
            try (var reader = new CdxReader(URI.create(cdxQueryUrl).toURL().openStream())) {
                var record = reader.next();
                if (record.isPresent()) {
                    log.debug("CDX index already contains {} at {}", url, record.get().date());
                    seenUrlHashes.putIfAbsent(urlHash, record.get().date().toEpochMilli());
                    return;
                }
            }
        }

        if (dryRun) {
            System.err.println("(dry-run) GET " + url + " " + contentType);
            return;
        }

        String target = uri.getRawPath();
        if (uri.getRawQuery() != null) target += "?" + uri.getRawQuery();
        var httpRequest = new HttpRequest.Builder("GET", target)
                .version(HTTP_1_0)
                .addHeader("Connection", "close")
                .addHeader("Host", uri.getHost())
                .addHeader("User-Agent", userAgent)
                .build();
        FetchResult result = failsafe.get(() -> fetchInner(uri, via, httpRequest));
        seenUrlHashes.putIfAbsent(urlHash, result.response().date().toEpochMilli());
    }

    @NotNull
    private FetchResult fetchInner(URI uri, String via, HttpRequest httpRequest) throws IOException {
        log.info("GET {}", uri);
        var startTimeMillis = System.currentTimeMillis();
        WarcWriter warcWriter = warcWriterSupplier.writer();
        var result = warcWriter.fetch(uri, httpRequest, null);
        var metadata = new WarcMetadata.Builder()
                .date(result.response().date())
                .targetURI(result.response().target())
                .concurrentTo(result.response().id())
                .fields(Map.of("via", List.of(via),
                        "fetchTimeMs", List.of(Long.toString(System.currentTimeMillis() - startTimeMillis))))
                .build();
        warcWriter.write(metadata);
        log.info("Status {} {} ({} bytes)", result.response().http().status(), result.response().payloadType(),
                result.response().http().body().size());
        return result;
    }
}
