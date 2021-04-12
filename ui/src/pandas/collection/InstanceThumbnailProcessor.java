package pandas.collection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gather.InstanceThumbnail;
import pandas.gather.InstanceThumbnailRepository;
import pandas.render.Browser;
import pandas.render.BrowserPool;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Service
public class InstanceThumbnailProcessor {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);
    private static final Logger log = LoggerFactory.getLogger(InstanceThumbnailProcessor.class);
    private static final OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(true).build();
    private final InstanceRepository instanceRepository;
    private final InstanceThumbnailRepository instanceThumbnailRepository;
    private final BrowserPool browserPool;

    public InstanceThumbnailProcessor(ThumbnailRepository thumbnailRepository, InstanceRepository instanceRepository, InstanceThumbnailRepository instanceThumbnailRepository) {
        this.instanceRepository = instanceRepository;
        this.instanceThumbnailRepository = instanceThumbnailRepository;
        this.browserPool = new BrowserPool();
    }

    @Scheduled(fixedDelayString = "${pandas.thumbnailProcessor.delay:86400000}", initialDelayString = "${pandas.thumbnailProcessor.initialDelay:0}")
    public synchronized void run() {
        log.info("Starting scheduled thumbnail processor");
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(8,8,1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        try {
            threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            while (true) {
                List<Instance> instances = instanceRepository.findWithoutThumbnails(PageRequest.of(0, 100));
                if (instances.isEmpty()) break;
                threadPool.invokeAll(instances.stream().map(t -> (Callable<InstanceThumbnail>)(() -> processAndSave(t)))
                        .collect(Collectors.toList()));
            }
            log.info("Done");
        } catch (InterruptedException e) {
            log.warn("ThumbnailProcessor interrupted", e);
        } finally {
            threadPool.shutdown();
        }
    }

    public InstanceThumbnail processAndSave(Instance instance) {
        InstanceThumbnail thumbnail = process(instance);
        instanceThumbnailRepository.save(thumbnail);
        return thumbnail;
    }

    public InstanceThumbnail process(Instance instance) {
        RetryTemplate retry = new RetryTemplate();
        retry.setBackOffPolicy(new ExponentialBackOffPolicy());
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        retry.setRetryPolicy(retryPolicy);
        retry.setThrowLastExceptionOnExhausted(true);
        try {
            return retry.execute(retryContext -> processOnce(instance));
        } catch (Exception e) {
            log.error("Error rendering " + instance.getId() + ": " + instance.getTepUrlAbsolute(), e);
            Instant now = Instant.now();
            InstanceThumbnail thumbnail = new InstanceThumbnail();
            thumbnail.setInstance(instance);
            thumbnail.setStatus(-1);
            thumbnail.setCreatedDate(now);
            thumbnail.setLastModifiedDate(now);
            thumbnail.setContentType("text/plain");
            thumbnail.setData(e.toString().getBytes(StandardCharsets.UTF_8));
            return thumbnail;
        }
    }

    private InstanceThumbnail processOnce(Instance instance) throws Exception {
        String url = instance.getTepUrlAbsolute();
        String sourceUrl = "https://web.archive.org.au/awa-nobanner/" + ARC_DATE.format(instance.getDate()) + "/" + url;
        log.info("Rendering instance {}: {}", instance.getId(), sourceUrl);
        Instant now = Instant.now();

        InstanceThumbnail thumbnail = new InstanceThumbnail();
        thumbnail.setInstance(instance);

        int width = 200;
        int cropWidth = 800;
        int cropHeight = 600;
        double scale = ((double) width) / cropWidth;

        var request = new Request.Builder().head().url(sourceUrl).build();
        try (var response = httpClient.newCall(request).execute()) {
            thumbnail.setStatus(response.code());
            if ("application/pdf".equalsIgnoreCase(response.header("Content-Type"))) {
                sourceUrl = "https://web.archive.org.au/webjars/pdf-js/web/viewer.html?file=" + UriUtils.encodeQueryParam(sourceUrl, StandardCharsets.UTF_8);
            }
        }

        var browser = browserPool.borrowObject();
        try (Browser.Tab tab = browser.createTab(cropWidth, cropHeight)) {
            try {
                tab.navigate(sourceUrl).get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Timeout loading {}", sourceUrl);
            }
            tab.hideScrollbars();
            thumbnail.setData(tab.screenshot(0, 0, cropWidth, cropHeight, scale));
        } finally {
            browserPool.returnObject(browser);
        }

        thumbnail.setContentType("image/jpeg");
        thumbnail.setCreatedDate(now);
        thumbnail.setLastModifiedDate(now);
        return thumbnail;
    }
}
