package pandas.gatherer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import pandas.browser.Browser;
import pandas.browser.BrowserPool;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gather.InstanceThumbnail;
import pandas.gather.InstanceThumbnailRepository;
import pandas.util.DateFormats;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

@Service
public class ThumbnailGenerator {
    private final Logger log = LoggerFactory.getLogger(ThumbnailGenerator.class);
    private final BrowserPool browserPool = new BrowserPool();
    private final InstanceRepository instanceRepository;
    private final InstanceThumbnailRepository instanceThumbnailRepository;
    private final TransactionTemplate transactionTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private final int timeoutSeconds = 30;

    public ThumbnailGenerator(InstanceRepository instanceRepository, InstanceThumbnailRepository instanceThumbnailRepository, TransactionTemplate transactionTemplate) {
        this.instanceRepository = instanceRepository;
        this.instanceThumbnailRepository = instanceThumbnailRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void generateLiveThumbnail(Instance instance) {
        if (instanceThumbnailRepository.existsByInstanceAndType(instance, InstanceThumbnail.Type.LIVE)) return;

        String url = instance.getTitle().getPrimarySeedUrl();
        InstanceThumbnail thumbnail;
        try {
            thumbnail = generateThumbnail(url);
        } catch (Exception e) {
            log.error("Error generating live thumbnail of " + url, e);
            return;
        }

        transactionTemplate.executeWithoutResult((status) -> {
            thumbnail.setType(InstanceThumbnail.Type.LIVE);
            thumbnail.setInstance(instanceRepository.findById(instance.getId()).orElseThrow());
            instanceThumbnailRepository.save(thumbnail);
        });
    }

    public void generateReplayThumbnail(Instance instance, String url) {
        if (instanceThumbnailRepository.existsByInstanceAndType(instance, InstanceThumbnail.Type.REPLAY)) return;

        InstanceThumbnail thumbnail;
        try {
            thumbnail = generateThumbnail(url);
        } catch (Exception e) {
            log.error("Error generating replay thumbnail of " + url, e);
            return;
        }

        transactionTemplate.executeWithoutResult((status) -> {
            thumbnail.setType(InstanceThumbnail.Type.REPLAY);
            thumbnail.setInstance(instanceRepository.findById(instance.getId()).orElseThrow());
            instanceThumbnailRepository.save(thumbnail);
        });
    }


    private InstanceThumbnail generate(Instance instance) throws Exception {
        String url = instance.getTepUrlAbsolute();
        String sourceUrl = "https://web.archive.org.au/awa-nobanner/" + DateFormats.ARC_DATE.format(instance.getDate()) + "/" + url;
        return generateThumbnail(sourceUrl);
    }

    InstanceThumbnail generateThumbnail(String url) throws Exception {
        log.info("Generating thumbnail for {}", url);
        Instant now = Instant.now();

        InstanceThumbnail thumbnail = new InstanceThumbnail();
        thumbnail.setCreatedDate(now);
        thumbnail.setLastModifiedDate(now);
        thumbnail.setContentType("image/jpeg");

        int width = 200;
        int height = 75;
        int cropWidth = 800;
        int cropHeight = 600;
        double scale = ((double) width) / cropWidth;

        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create(url)).method("HEAD", noBody())
                        .timeout(Duration.ofSeconds(timeoutSeconds)).build(),
                BodyHandlers.discarding());

        thumbnail.setStatus(response.statusCode());

        if ("application/pdf".equalsIgnoreCase(response.headers().firstValue("Content-Type").orElse(""))) {
            return generatePdfThumbnail(url, thumbnail, width, height);
        }

        var browser = browserPool.borrowObject();
        try (Browser.Tab tab = browser.createTab(cropWidth, cropHeight)) {
            try {
                tab.navigate(url).get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Timeout loading {}", url);
            }
            tab.hideScrollbars();
            Thread.sleep(500); // give JS a moment to run
            thumbnail.setData(tab.screenshot(0, 0, cropWidth, cropHeight, scale));
        } finally {
            browserPool.returnObject(browser);
        }
        return thumbnail;
    }

    private InstanceThumbnail generatePdfThumbnail(String url, InstanceThumbnail thumbnail, int width, int height) throws IOException, InterruptedException {
        try (var body = httpClient.send(HttpRequest.newBuilder(URI.create(url)).GET()
                .timeout(Duration.ofSeconds(timeoutSeconds)).build(), BodyHandlers.ofInputStream()).body()) {
            Process process = new ProcessBuilder("gs", "-sstdout=%stderr", "-q", "-sDEVICE=jpeg",
                    "-dNOPAUSE", "-dBATCH", "-r35x36", "-dPDFFitPage=true", "-sPageList=1",
                    "-dTextAlphaBits=4", "-dGraphicsAlphaBits=4",
                    "-dDEVICEWIDTH=" + width, "-dDEVICEHEIGHT=" + height, "-sOutputFile=-", "-")
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            try {
                body.transferTo(process.getOutputStream());
                process.getOutputStream().close();
                thumbnail.setData(process.getInputStream().readAllBytes());
                if (process.waitFor() != 0) {
                    throw new IOException("ghostscript returned " + process.exitValue());
                }
                return thumbnail;
            } finally {
                process.destroyForcibly();
            }
        }
    }
}
