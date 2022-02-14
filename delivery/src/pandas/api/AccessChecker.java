package pandas.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pandas.util.DateFormats;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class AccessChecker {
    private static URL accessCheckUrl;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private List<Decision> queryBulk(List<Query> queries) throws IOException {
        if (accessCheckUrl == null) {
            return Collections.emptyList();
        }
        HttpURLConnection connection = (HttpURLConnection) accessCheckUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        objectMapper.writeValue(connection.getOutputStream(), queries);
        return objectMapper.readValue(connection.getInputStream(), new TypeReference<>() {});
    }

    public void checkAccessBulk(List<Restrictable> objects) throws IOException {
        List<Query> queries = objects.stream().map(Restrictable::toAccessQuery).toList();
        var decisions = queryBulk(queries);
        var objectIterator = objects.iterator();
        var decisionIterator = decisions.iterator();
        while (objectIterator.hasNext() && decisionIterator.hasNext()) {
            objectIterator.next().handleAccessDecision(decisionIterator.next());
        }
    }

    interface Restrictable {
        Query toAccessQuery();
        void handleAccessDecision(Decision decision);
    }

    public static class Query {
        public final String url;
        public final String timestamp;

        public Query(String url, Instant timestamp) {
            this.url = url;
            this.timestamp = DateFormats.ARC_DATE.format(timestamp);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Decision {
        private boolean allowed;
        private Rule rule;

        public boolean isAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        public Rule getRule() {
            return rule;
        }

        public void setRule(Rule rule) {
            this.rule = rule;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rule {
        private String publicMessage;

        public String getPublicMessage() {
            return publicMessage;
        }

        public void setPublicMessage(String publicMessage) {
            this.publicMessage = publicMessage;
        }
    }
}
