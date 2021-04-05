package pandas.gatherer.heritrix;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BambooClient {
    private final BambooConfig config;
    private final WebClient webClient;

    public BambooClient(BambooConfig config, ReactiveClientRegistrationRepository clientRegistrations) {
        this.config = config;
        var clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
        var clientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
        var oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth.setDefaultClientRegistrationId("oidc");
        webClient = WebClient.builder()
                .baseUrl(config.getUrl())
                .filter(oauth)
                .filter(ExchangeFilterFunction.ofRequestProcessor(req -> {
                    System.err.println(req.headers());
                    return Mono.just(req);
                }))
                .build();


        System.out.println(webClient.get().uri(b -> b.path("crawls").build()).retrieve().bodyToMono(String.class).block());
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
}
