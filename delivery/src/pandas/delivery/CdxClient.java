package pandas.delivery;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pandas.gather.Instance;
import pandas.util.Dates;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class CdxClient {
    private final WebClient webClient = WebClient.create("http://winch.nla.gov.au:9901/trove/ap/public");

    public Map<Instance,Decision> checkInstanceRestrictions(List<Instance> instances) {
        var map = new HashMap<Instance,Decision>();
        var decisions = checkPermissions(instances.stream().map(Snapshot::new).collect(Collectors.toList()));
        var instanceIterator = instances.iterator();
        var decisionIterator = decisions.iterator();
        while (instanceIterator.hasNext() && decisionIterator.hasNext()) {
            map.put(instanceIterator.next(), decisionIterator.next());
        }
        return map;
    }

    public List<Decision> checkPermissions(List<Snapshot> snapshots) {
        return webClient.post()
                .uri("/check")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(Mono.just(snapshots), new ParameterizedTypeReference<>() {})
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Decision>>() {}).block();
    }

    public static class Snapshot {
        public final String url;
        public final String timestamp;

        public Snapshot(Instance instance) {
            url = instance.getTepUrlAbsolute();
            timestamp = Dates.ARC_DATE.format(instance.getDate());
        }

        public Snapshot(String url, String timestamp) {
            this.url = url;
            this.timestamp = timestamp;
        }
    }

    public static class Decision {
        public boolean allowed;
    }
}
