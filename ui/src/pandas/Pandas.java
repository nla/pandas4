package pandas;

import okhttp3.OkHttpClient;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class Pandas {
    public static void main(String[] args) {
        copyEnvToProperty("OIDC_URL", "spring.security.oauth2.client.provider.oidc.issuer-uri");
        copyEnvToProperty("OIDC_CLIENT_ID", "spring.security.oauth2.client.registration.oidc.client-id");
        copyEnvToProperty("OIDC_CLIENT_SECRET", "spring.security.oauth2.client.registration.oidc.client-secret");
        SpringApplication.run(Pandas.class, args);
    }

    private static void copyEnvToProperty(String env, String property) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) {
            System.setProperty(property, value);
        }
    }

    @Bean(name = "htmlSanitizer")
    public PolicyFactory htmlSanitizer() {
        return Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.LINKS).and(Sanitizers.TABLES);
    }

    @Bean(name = "httpClient")
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder().build();
    }
}
