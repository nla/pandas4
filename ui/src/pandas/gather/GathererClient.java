package pandas.gather;

import org.springframework.stereotype.Service;
import pandas.core.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GathererClient {
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
        httpClient.sendAsync(HttpRequest.newBuilder(URI.create(gathererUrl + "/pause"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString())
                .completeOnTimeout(null, 5, TimeUnit.SECONDS)
                .exceptionally(ex -> null);
    }

    public void unpause() {
        String gathererUrl = config.getGathererUrl();
        if (gathererUrl == null) return;
        httpClient.sendAsync(HttpRequest.newBuilder(URI.create(gathererUrl + "/unpause"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString())
                .completeOnTimeout(null, 5, TimeUnit.SECONDS)
                .exceptionally(ex -> null);
    }
}
