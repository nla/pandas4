package pandas.render;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "llm")
public record LLMConfig(
        URI url,
        String apiKey,
        String model) {
    public static LLMConfig fromEnv() {
        return new LLMConfig(URI.create(System.getenv("LLM_URL")),
                System.getenv("LLM_API_KEY"),
                System.getenv("LLM_MODEL"));
    }
}
