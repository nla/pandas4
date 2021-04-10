package pandas.gatherer.heritrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

@Component
public class BambooClient {
    public static final MediaType APPLICATION_WARC = MediaType.parseMediaType("application/warc");
    private final BambooConfig config;
    private final WebClient webClient;

    public BambooClient(BambooConfig config, @Autowired(required = false) ReactiveClientRegistrationRepository clientRegistrations) {
        this.config = config;

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(config.getUrl());

        if (clientRegistrations != null) {
            var clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
            var clientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
            var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
            oauth.setDefaultClientRegistrationId("oidc");
            builder.filter(oauth);
        }
        webClient = builder
                .filter(ExchangeFilterFunction.ofRequestProcessor(req -> {
                    System.err.println(req.headers());
                    return Mono.just(req);
                }))
                .build();
    }

    public Long getCrawlIdForInstance(long instanceId) {
        var response = webClient.get().uri(b -> b.path("instances/{id}").build(instanceId))
                .retrieve().toBodilessEntity().block();
        if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) return null;
        return Long.parseLong(response.getHeaders().getLocation().getPath().replaceFirst(".*/",""));
    }

    public long createCrawl(String name, Long instanceId) {
        var response = webClient.post()
                .uri(b -> b.path("crawls/new").build())
                .body(BodyInserters.fromFormData("name", name)
                        .with("crawlSeriesId", config.getCrawlSeriesId().toString())
                        .with("pandasInstanceId", instanceId.toString()))
                .retrieve().toBodilessEntity().block();
        if (response.getStatusCode().isError()) throw new RuntimeException("create crawl returned " + response.getStatusCode());
        return Long.parseLong(response.getHeaders().getLocation().getPath().replaceFirst(".*/", ""));
    }

    public long getOrCreateCrawl(String name, Long instanceId) {
        Long crawlId = getCrawlIdForInstance(instanceId);
        if (crawlId != null) return crawlId;
        return createCrawl(name, instanceId);
    }

    public boolean warcExists(long crawlId, String filename) {
        var response = webClient.head()
                .uri(b -> b.path("crawls/{id}/warcs/{filename}").build(crawlId, filename))
                .retrieve().toBodilessEntity().block();
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } if (response.getStatusCode().value() == 404) {
            return false;
        } else {
            throw new RuntimeException("bamboo returned unexpected response code " + response.getStatusCode());
        }
    }

    public void putWarcIfNotExists(long crawlId, String filename, Path file) {
        if (warcExists(crawlId, filename)) return;
        var response = webClient.put()
                .uri(b -> b.path("crawls/{id}/warcs/{filename}").build(crawlId, filename))
                .contentType(APPLICATION_WARC)
                .body(BodyInserters.fromResource(new FileSystemResource(file)))
                .retrieve().toBodilessEntity().block();
        if (response.getStatusCode().isError()) {
            throw new RuntimeException("bamboo returned " + response.getStatusCode());
        }
    }

    public boolean artifactExists(long crawlId, String relpath) {
        var response = webClient.head()
                .uri(b -> b.path("crawls/{id}/artifacts/{relpath}").build(crawlId, relpath))
                .retrieve().toBodilessEntity().block();
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } if (response.getStatusCode().value() == 404) {
            return false;
        } else {
            throw new RuntimeException("bamboo returned unexpected response code " + response.getStatusCode());
        }
    }

    public void putArtifactIfNotExists(long crawlId, String relpath, Path file) {
        if (artifactExists(crawlId, relpath)) return;
        var response = webClient.put()
                .uri(b -> b.path("crawls/{id}/artifacts/{relpath}").build(crawlId, relpath))
                .body(BodyInserters.fromResource(new FileSystemResource(file)))
                .retrieve().toBodilessEntity().block();
        if (response.getStatusCode().isError()) {
            throw new RuntimeException("bamboo returned " + response.getStatusCode());
        }
    }
}
