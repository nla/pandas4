package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import pandas.browser.Browser;
import pandas.browser.BrowserPool;
import pandas.util.DateFormats;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ThumbnailProcessor {
    private static final Logger log = LoggerFactory.getLogger(ThumbnailProcessor.class);
    private final HttpClient httpClient;
    private final TitleRepository titleRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final BrowserPool browserPool;

    public ThumbnailProcessor(HttpClient httpClient, TitleRepository titleRepository, ThumbnailRepository thumbnailRepository) {
        this.httpClient = httpClient;
        this.titleRepository = titleRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.browserPool = new BrowserPool();
    }

    public synchronized void run() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(8,8,1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        try {
            threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            while (true) {
                List<Title> titles = titleRepository.findWithoutThumbnails(PageRequest.of(0, 100));
                if (titles.isEmpty()) break;
                threadPool.invokeAll(titles.stream().map(t -> (Callable<Thumbnail>)(() -> processAndSave(t)))
                        .toList());
            }
        } catch (InterruptedException e) {
            log.warn("ThumbnailProcessor interrupted", e);
        } finally {
            threadPool.shutdown();
        }
    }

    private Thumbnail processAndSave(Title title) {
        Thumbnail thumbnail = process(title);
        thumbnailRepository.save(thumbnail);
        return thumbnail;
    }

    public Thumbnail process(Title title) {
        RetryTemplate retry = new RetryTemplate();
        retry.setBackOffPolicy(new ExponentialBackOffPolicy());
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        retry.setRetryPolicy(retryPolicy);
        retry.setThrowLastExceptionOnExhausted(true);
        try {
            return retry.execute(retryContext -> processOnce(title));
        } catch (Exception e) {
            log.error("Error rendering " + title.getId() + ": " + title.getTitleUrl(), e);
            Instant now = Instant.now();
            Thumbnail thumbnail = new Thumbnail();
            thumbnail.setPriority(100000);
            thumbnail.setTitle(title);
            thumbnail.setStatus(-1);
            thumbnail.setCropX(0);
            thumbnail.setCropY(0);
            thumbnail.setWidth(0);
            thumbnail.setHeight(0);
            thumbnail.setCropWidth(0);
            thumbnail.setCropHeight(0);
            thumbnail.setUrl(title.getTitleUrl());
            thumbnail.setDate(now);
            thumbnail.setCreatedDate(now);
            thumbnail.setLastModifiedDate(now);
            thumbnail.setContentType("text/plain");
            thumbnail.setData(e.toString().getBytes(StandardCharsets.UTF_8));
            return thumbnail;
        }
    }

    private Thumbnail processOnce(Title title) throws Exception {
        String url = title.getSeedUrl();
        if (url == null) url = title.getTitleUrl();
        String sourceUrl = "https://web.archive.org.au/awa-nobanner/20130328232628/" + url;
        log.info("Rendering title {}: {}", title.getId(), sourceUrl);
        Instant now = Instant.now();

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setWidth(200);
        thumbnail.setHeight(200);
        thumbnail.setCropX(0);
        thumbnail.setCropY(0);
        thumbnail.setCropWidth(800);
        thumbnail.setCropHeight(600);
        double scale = ((double) thumbnail.getWidth()) / thumbnail.getCropWidth();
        thumbnail.setHeight((int)(thumbnail.getCropHeight() * scale));

        var request = HttpRequest.newBuilder().method("HEAD", HttpRequest.BodyPublishers.noBody()).uri(URI.create(sourceUrl)).build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        thumbnail.setStatus(response.statusCode());
        thumbnail.setSourceType(response.headers().firstValue("Content-Type").orElse(null));
        if ("application/pdf".equalsIgnoreCase(thumbnail.getSourceType())) {
            sourceUrl = "https://web.archive.org.au/webjars/pdf-js/web/viewer.html?file=" + UriUtils.encodeQueryParam(sourceUrl, StandardCharsets.UTF_8);
        }


        var browser = browserPool.borrowObject();
        try (Browser.Tab tab = browser.createTab(thumbnail.getCropWidth(), thumbnail.getCropHeight())) {
            try {
                tab.navigate(sourceUrl).get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Timeout loading {}", sourceUrl);
            }
            tab.hideScrollbars();
            String timestamp = tab.eval("if (typeof wbinfo === 'undefined') { return null; } else { return wbinfo.timestamp; }").getString("result");
            if (timestamp != null) {
                thumbnail.setDate(DateFormats.ARC_DATE.parse(timestamp, Instant::from));
            } else {
                thumbnail.setDate(now);
            }
            thumbnail.setData(tab.screenshot(thumbnail.getCropX(), thumbnail.getCropY(), thumbnail.getCropWidth(),
                    thumbnail.getCropHeight(), scale));
        } finally {
            browserPool.returnObject(browser);
        }

        thumbnail.setPriority(0);
        thumbnail.setTitle(title);
        thumbnail.setUrl(url);
        thumbnail.setContentType("image/jpeg");
        thumbnail.setCreatedDate(now);
        thumbnail.setLastModifiedDate(now);
        return thumbnail;
    }
}
