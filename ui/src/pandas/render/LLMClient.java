package pandas.render;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

@Service
@ConditionalOnProperty("llm.url")
public class LLMClient {
    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .build();
    private final LLMConfig config;

    public LLMClient(LLMConfig config) {
        this.config = config;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(SnakeCaseStrategy.class)
    public static class Request {
        public String model;
        public List<Message> messages;
        public Object jsonSchema;
        public double temperature;
        public Integer maxTokens;
        public Request(String model, String message, Object jsonSchema) {
            this.model = model;
            this.messages = List.of(new Message("user", message));
            this.jsonSchema = jsonSchema;
        }
    }

    public Request newRequest(String message) {
        return new Request(config.model(), message, null);
    }

    record Message(String role, String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Response(List<Choice> choices, Usage usage) {
        public String message() {
            if (choices.isEmpty()) return null;
            return choices.get(0).message().content();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(SnakeCaseStrategy.class)
    record Choice(Message message, String finishReason) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    record Usage(int promptTokens, int completionTokens, int totalTokens) {
    }

    public String chat(String message, Object jsonSchema) throws IOException, InterruptedException {
        var request = new Request(config.model(), message, jsonSchema);
        var response = chat(request);
        if (response.choices().isEmpty()) throw new IOException("No choices in LLM response");
        return response.choices().get(0).message().content();
    }

    public String chat(String message) throws IOException, InterruptedException {
       return chat(message, null);
    }

    public Response chat(Request request) throws IOException, InterruptedException {
        var requestBody = objectMapper.writeValueAsString(request);
        log.info("LLM request: {}", requestBody);
        var httpRequestBuilder = HttpRequest.newBuilder(config.url().resolve("v1/chat/completions"))
                .POST(BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(30));
        if (config.apiKey() != null) {
            httpRequestBuilder.header("Authorization", "Bearer " + config.apiKey());
        }
        var httpRequest = httpRequestBuilder.build();
        var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.info("LLM response: {}", httpResponse.body());
        var response = objectMapper.readValue(httpResponse.body(), Response.class);
        return response;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String message = String.join(" ", args);
    }
}
