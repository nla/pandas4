package pandas.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletResponse;
import javax.validation.constraints.Pattern;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Controller
public class TranscodeController {
    private static final Logger log = LoggerFactory.getLogger(TranscodeController.class);

    @GetMapping("/transcode")
    public void transcode(@RequestParam("date") @Pattern(regexp = "[0-9]+") String date,
                          @RequestParam("url") String url,
                          ServletResponse response) throws IOException, InterruptedException {
        if (url.contains("/../")) throw new IllegalArgumentException("url cannot contain ..");
        URL archiveUrl = new URL("https://web.archive.org.au/awa/" + date + "id_/" + url);
        log.info("Transcoding {}", archiveUrl);
        HttpURLConnection connection = (HttpURLConnection) archiveUrl.openConnection();
        connection.setInstanceFollowRedirects(true);
        log.info("-> " + connection.getResponseCode() + " " + connection.getContentType());
        try (InputStream sourceStream = connection.getInputStream()) {
            Process process = new ProcessBuilder("ffmpeg", "-i", "-", "-f", "webm", "-c:v", "libvpx",
                    "-c:a", "libopus", "-hide_banner", "-loglevel", "error", "-")
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            try {
                new Thread(() -> {
                    Thread.currentThread().setName("TranscodingInputThread " + archiveUrl);
                    try (sourceStream;
                         OutputStream outputStream = process.getOutputStream()) {
                        sourceStream.transferTo(outputStream);
                    } catch (IOException e) {
                        if (e.getMessage().equals("Stream Closed")) return;
                        log.error("Error transcoding " + archiveUrl, e);
                    }
                }).start();
                response.setContentType("video/webm");
                process.getInputStream().transferTo(response.getOutputStream());
            } finally {
                log.info("Closing " + archiveUrl);
                process.getOutputStream().close();
                process.getInputStream().close();
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroy();
                    if (!process.waitFor(2, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
        }
    }
}