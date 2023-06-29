package pandas.gather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import pandas.core.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GathererClient {
    private final static Logger log = LoggerFactory.getLogger(GathererClient.class);
    private final Config config;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GathererClient(Config config) {
        this.config = config;
    }

    public CompletableFuture<String> statusAsync() {
        String gathererUrl = config.getGathererUrl();
        if (gathererUrl == null) return CompletableFuture.completedFuture("Gatherer not configured");
        return httpClient.sendAsync(HttpRequest.newBuilder(URI.create(gathererUrl)).build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(body -> body.statusCode() != 200 ? "Error " + body.statusCode()  :
                        body.body().replaceFirst("(?s)<.*", ""))
                .completeOnTimeout("Gatherer not responding", 5, TimeUnit.SECONDS)
                .exceptionally(ex -> "Error contacting gatherer: " + ex);
    }

    public void pause() {
        String gathererUrl = config.getGathererUrl();
        if (gathererUrl == null) return;
        httpClient.sendAsync(HttpRequest.newBuilder(makeUri( "/pause"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString())
                .completeOnTimeout(null, 5, TimeUnit.SECONDS)
                .exceptionally(ex -> null);
    }

    public void unpause() {
        String gathererUrl = config.getGathererUrl();
        if (gathererUrl == null) return;
        httpClient.sendAsync(HttpRequest.newBuilder(makeUri( "/unpause"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString())
                .completeOnTimeout(null, 5, TimeUnit.SECONDS)
                .exceptionally(ex -> null);
    }

    public void replaceAllInInstance(Instance instance, FindAndReplaceForm form, String email) throws InterruptedException, IOException {
        Objects.requireNonNull(email, "email address is required");
        URI uri = makeUri("/replaceAllInInstance");
        String body = form.encode(instance.getId(), email);
        log.info("Sending POST {} {}", uri, body);
        var response = httpClient.send(HttpRequest.newBuilder(uri)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Error " + response.statusCode() + " from gatherer: " + response.body());
        }
    }

    private URI makeUri(String path) {
        return URI.create(config.getGathererUrl().replaceFirst("/+$", "") + path);
    }
}
