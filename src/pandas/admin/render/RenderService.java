package pandas.admin.render;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.discarding;

@Service
public class RenderService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public Render render(String url) {
        HttpResponse<Void> response;
        try {
            response = httpClient.send(HttpRequest.newBuilder(URI.create(url))
                    .method("HEAD", noBody())
                    .build(), discarding());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Status " + response.statusCode() + " for " + url);
        }

        ChromeDriver chromeDriver = new ChromeDriver(new ChromeOptions().setHeadless(true));
        try {
            chromeDriver.get(url);
            hideScrollbar(chromeDriver);
            Render render = new Render();
            render.setRenderDate(new Date());
            render.setUrl(url);
            render.setTitle(chromeDriver.getTitle());

            List<RenderImage> images = new ArrayList<>();
            images.add(screenshot(url, chromeDriver));
            images.addAll(topImages(chromeDriver));
            render.setImages(images);
            return render;
        } finally {
            chromeDriver.quit();
        }
    }

    @NotNull
    private RenderImage screenshot(String url, ChromeDriver chromeDriver) {
        RenderImage screenshot = new RenderImage();
        screenshot.setType(RenderImageType.VIEWPORT);
        screenshot.setSrc(url);
        screenshot.setDataUrl("data:base64;" + chromeDriver.getScreenshotAs(OutputType.BASE64));
        return screenshot;
    }

    private void hideScrollbar(ChromeDriver chromeDriver) {
        chromeDriver.executeScript("document.getElementsByTagName('body')[0].style.overflow='hidden'");
    }

    private List<RenderImage> topImages(ChromeDriver chromeDriver) {
        int nImages = 5;
        int minDimension = 50;
        int thumbnailDimension = 100;
        Object images = chromeDriver.executeScript("" +
                "let minDimension = " + minDimension + ";\n" +
                "let nImages = " + nImages + ";\n" +
                "let thumbnailDimension = " + thumbnailDimension + ";\n" +
                "return Array.from(document.getElementsByTagName('img'))\n" +
                "    .sort((a, b) => b.width * b.height - a.width - b.height)\n" +
                "    .filter(img => img.width >= minDimension && img.height >= minDimension)\n" +
                "    .slice(0, nImages)\n" +
                "    .map(img => {\n" +
                "        let c = document.createElement('canvas');\n" +
                "        if (img.width > img.height) {\n" +
                "            c.width = thumbnailDimension;\n" +
                "            c.height = c.width / img.width * img.height;\n" +
                "        } else {\n" +
                "            c.height = thumbnailDimension;\n" +
                "            c.width = c.height / img.height * img.width;\n" +
                "        }\n" +
                "        let ctx = c.getContext('2d');\n" +
                "        ctx.drawImage(img, 0, 0, c.width, c.height);\n" +
                "        return {src: img.currentSrc,\n" +
                "                dataUrl: c.toDataURL('image/webp'),\n" +
                "                type: 'IMG'};\n" +
                "    });");
        return objectMapper.convertValue(images, new TypeReference<List<RenderImage>>() {});
    }
}
