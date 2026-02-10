package pandas.gatherer.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Component
@ConditionalOnProperty("bamboo.url")
public class BambooClient implements HealthIndicator {
    public static final MediaType APPLICATION_WARC = MediaType.parseMediaType("application/warc");
    private final BambooConfig config;
    private final WebClient webClient;

    public BambooClient(BambooConfig config, @Autowired(required = false) ReactiveClientRegistrationRepository clientRegistrations) {
        this.config = config;

        String baseUrl = config.getUrl();
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl);

        if (clientRegistrations != null) {
            var clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
            var clientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
            var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
            oauth.setDefaultClientRegistrationId("oidc");
            builder.filter(oauth);
        }
        webClient = builder
//                .filter(ExchangeFilterFunction.ofRequestProcessor(req -> {
//                    System.err.println(req.headers());
//                    return Mono.just(req);
//                }))
                .build();
    }

    public Long getCrawlIdForInstance(long instanceId) {
        return webClient.get().uri(b -> b.path("instances/{id}").build(instanceId))
                .retrieve()
                .toBodilessEntity()
                .map(this::locationToId)
                .onErrorResume(WebClientResponseException.NotFound.class, t -> Mono.empty())
                .block();
    }

    @SuppressWarnings("ConstantConditions")
    public long createCrawl(String name, Long instanceId) {
        return webClient.post()
                .uri(b -> b.path("crawls/new").build())
                .body(fromFormData("name", name)
                        .with("crawlSeriesId", config.getCrawlSeriesId().toString())
                        .with("pandasInstanceId", instanceId.toString()))
                .retrieve().toBodilessEntity()
                .map(this::locationToId)
                .block();
    }

    public long getOrCreateCrawl(String name, Long instanceId) {
        Long crawlId = getCrawlIdForInstance(instanceId);
        if (crawlId != null) return crawlId;
        return createCrawl(name, instanceId);
    }

    public boolean warcExists(long crawlId, String filename) {
        return webClient.head()
                .uri(b -> b.path("crawls/{id}/warcs/{filename}").build(crawlId, filename))
                .retrieve().toBodilessEntity()
                .onErrorResume(WebClientResponseException.NotFound.class, t -> Mono.empty())
                .block() != null;
    }

    public void putWarcIfNotExists(long crawlId, String filename, Path file) throws IOException {
        if (warcExists(crawlId, filename)) return;
        webClient.put()
                .uri(b -> b.path("crawls/{id}/warcs/{filename}").build(crawlId, filename))
                .contentType(APPLICATION_WARC)
                .body(fileBody(file))
                .retrieve().toBodilessEntity().block();
    }

    public boolean artifactExists(long crawlId, String relpath) {
        return webClient.head()
                .uri(b -> b.path("crawls/{id}/artifacts/" + relpath).build(crawlId))
                .retrieve().toBodilessEntity()
                .onErrorResume(WebClientResponseException.NotFound.class, t -> Mono.empty())
                .block() != null;
    }

    public void putArtifactIfNotExists(long crawlId, String relpath, Path file) throws IOException {
        if (artifactExists(crawlId, relpath)) return;
        webClient.put()
                .uri(b -> b.path("crawls/{id}/artifacts/" + relpath).build(crawlId))
                .body(fileBody(file))
                .retrieve().toBodilessEntity().block();
    }

    private long locationToId(ResponseEntity<Void> rsp) {
        URI uri = Objects.requireNonNull(rsp.getHeaders().getLocation(), "missing location header");
        return Long.parseLong(uri.getPath().replaceFirst(".*/", ""));
    }

    private BodyInserter<?, ? super ClientHttpRequest> fileBody(Path file) throws IOException {
        if (Files.size(file) == 0) {
            // if the file is empty netty mysteriously throws "IllegalStateException: unexpected message type: DefaultFileRegion"
            // so as a workaround insert a blank body explicitly
            return BodyInserters.empty();
        }
        return BodyInserters.fromResource(new FileSystemResource(file));
    }

    private void checkSeries() {
        webClient.get()
                .uri(b -> b.path("series/{id}").build(config.getCrawlSeriesId()))
                .retrieve().toBodilessEntity().block();
    }

    @Override
    public Health health() {
        long start = System.currentTimeMillis();
        checkSeries();
        return Health.up().withDetail("latencyMillis", System.currentTimeMillis() - start).build();
    }
}
