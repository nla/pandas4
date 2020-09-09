package pandas.admin.collection;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.util.UriUtils;
import pandas.admin.render.Browser;
import pandas.admin.render.BrowserPool;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.ZoneOffset.UTC;

public class ThumbnailProcessor implements ItemProcessor<Title, Thumbnail>, ItemStream {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);
    private static final Logger log = LoggerFactory.getLogger(ThumbnailProcessor.class);
    private static final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    private GenericObjectPool<Browser> browserPool;

    @Override
    public Thumbnail process(Title title) throws Exception {
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

        var request = HttpRequest.newBuilder(URI.create(sourceUrl)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            thumbnail.setStatus(response.statusCode());
            thumbnail.setSourceType(response.headers().firstValue("Content-Type").orElse(null));
            if ("application/pdf".equalsIgnoreCase(thumbnail.getSourceType())) {
                sourceUrl = "https://web.archive.org.au/webjars/pdf-js/web/viewer.html?file=" + UriUtils.encodeQueryParam(sourceUrl, StandardCharsets.UTF_8);
            }

        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
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
                thumbnail.setDate(ARC_DATE.parse(timestamp, Instant::from));
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

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.browserPool = new BrowserPool();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        browserPool.close();
    }

    public static BufferedImage stripAlphaChannel(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) return image;

        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setColor(new Color(0, false));
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return target;
    }
}
