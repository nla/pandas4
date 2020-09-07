package pandas.admin.collection;

import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.time.ZoneOffset.UTC;

public class ThumbnailProcessor implements ItemProcessor<Title, Thumbnail>, ItemStream {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);

    private static final Logger log = LoggerFactory.getLogger(ThumbnailProcessor.class);
    private ChromeDriver chromeDriver;
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

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
    private Thumbnail processOnce(Title title) throws IOException, InterruptedException {
        String sourceUrl = "https://web.archive.org.au/awa-nobanner/20130328232628/" + title.getTitleUrl();
        log.info("Rendering title {}: {}", title.getId(), sourceUrl);
        Instant now = Instant.now();

        int status = sendHeadRequest(sourceUrl);

        chromeDriver.get(sourceUrl);
        Wbinfo wbinfo = new Wbinfo(chromeDriver);
        byte[] imageData = chromeDriver.getScreenshotAs(OutputType.BYTES);

        var image = ImageIO.read(new ByteArrayInputStream(imageData));
        var scaled = stripAlphaChannel(Scalr.resize(image, 200, 200));
        var baos = new ByteArrayOutputStream();
        boolean ok = ImageIO.write(scaled, "jpeg", baos);
        if (!ok) throw new RuntimeException("ImageIO.write failed");
        byte[] scaledData = baos.toByteArray();

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setPriority(0);
        thumbnail.setStatus(status);
        thumbnail.setTitle(title);
        thumbnail.setUrl(title.getTitleUrl());
        thumbnail.setDate(wbinfo.date);
        thumbnail.setCropX(0);
        thumbnail.setCropY(0);
        thumbnail.setCropWidth(image.getWidth());
        thumbnail.setCropHeight(image.getHeight());
        thumbnail.setWidth(scaled.getWidth());
        thumbnail.setHeight(scaled.getHeight());
        thumbnail.setUrl(sourceUrl);
        thumbnail.setContentType("image/jpeg");
        thumbnail.setData(scaledData);
        thumbnail.setCreatedDate(now);
        thumbnail.setLastModifiedDate(now);
        return thumbnail;
    }

    private int sendHeadRequest(String sourceUrl) throws java.io.IOException, InterruptedException {
        return httpClient.send(HttpRequest.newBuilder(URI.create(sourceUrl))
                .method("HEAD", noBody())
                .build(), discarding()).statusCode();
    }


    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.warn("YOYOYO {}", executionContext);
        this.chromeDriver = new ChromeDriver(new ChromeOptions().setHeadless(true));
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        chromeDriver.quit();
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
