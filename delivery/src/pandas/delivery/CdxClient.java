package pandas.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pandas.gather.Instance;
import pandas.util.DateFormats;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class CdxClient {
    public CdxClient(@Value("${CDX_URL}") String cdxUrl) {
        webClient = WebClient.create(cdxUrl);
    }

    private final WebClient webClient;

    public Map<Instance,Decision> checkInstanceRestrictions(List<Instance> instances) {
        var map = new HashMap<Instance,Decision>();
        var decisions = checkPermissions(instances.stream().map(Snapshot::new).toList());
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
            timestamp = DateFormats.ARC_DATE.format(instance.getDate());
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
