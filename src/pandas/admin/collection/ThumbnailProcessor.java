package pandas.admin.collection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import pandas.admin.render.Browser;
import pandas.admin.render.BrowserPool;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static java.time.ZoneOffset.UTC;

public class ThumbnailProcessor implements ItemProcessor<Title, Thumbnail>, ItemStream {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);

    private static final Logger log = LoggerFactory.getLogger(ThumbnailProcessor.class);
    private static final OkHttpClient httpClient = new OkHttpClient();
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

    @NotNull
    private Thumbnail processOnce(Title title) throws Exception {
        String url = title.getSeedUrl();
        if (url == null) url = title.getTitleUrl();
        String sourceUrl = "https://web.archive.org.au/awa-nobanner/20130328232628/" + url;
        log.info("Rendering title {}: {}", title.getId(), sourceUrl);
        Instant now = Instant.now();

        int status = headRequest(sourceUrl);

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setWidth(200);
        thumbnail.setHeight(200);
        thumbnail.setCropX(0);
        thumbnail.setCropY(0);
        thumbnail.setCropWidth(800);
        thumbnail.setCropHeight(600);
        double scale = ((double) thumbnail.getWidth()) / thumbnail.getCropWidth();
        thumbnail.setHeight((int)(thumbnail.getCropHeight() * scale));


        byte[] imageData;
        var browser = browserPool.borrowObject();
        try (Browser.Tab tab = browser.createTab(thumbnail.getCropWidth(), thumbnail.getCropHeight())) {
            tab.navigate(sourceUrl);
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
        thumbnail.setStatus(status);
        thumbnail.setTitle(title);
        thumbnail.setUrl(url);
        thumbnail.setContentType("image/jpeg");
        thumbnail.setCreatedDate(now);
        thumbnail.setLastModifiedDate(now);
        return thumbnail;
    }

    private int headRequest(String sourceUrl) throws IOException {
        Request request = new Request.Builder().url(sourceUrl).head().build();
        try (Response response = httpClient.newCall(request).execute()) {
            return response.code();
        }
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

    public static class Wbinfo {
        private static final Pattern REGEX = Pattern.compile(".*?/([0-9]{14})[a-z_]*/(.*)");

        private final String url;
        private final Instant date;

        @SuppressWarnings("unchecked")
        Wbinfo(ChromeDriver chromeDriver) {
            Object wbinfoRaw = chromeDriver.executeScript("if (typeof wbinfo === 'undefined') { return null; } else { return wbinfo; }");
            if (wbinfoRaw instanceof Map) {
                var map = (Map<String,Object>) wbinfoRaw;
                this.date = ARC_DATE.parse((String)map.get("timestamp"), Instant::from);
                this.url = (String)map.get("url");
            } else {
                String currentUrl = (String) chromeDriver.executeScript("return document.location.href");
                var m = REGEX.matcher(currentUrl);
                if (m.matches()) {
                    date = ARC_DATE.parse(m.group(1), Instant::from);
                    url = m.group(2);
                } else {
                    date = Instant.now();
                    url = chromeDriver.getCurrentUrl();
                }
            }
        }
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
