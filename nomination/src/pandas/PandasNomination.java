package pandas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PandasNomination {
    public static void main(String[] args) {
        copyEnvToProperty("SHIRE_URL", "spring.security.oauth2.client.provider.shire.issuer-uri");
        copyEnvToProperty("SHIRE_CLIENT_ID", "spring.security.oauth2.client.registration.shire.client-id");
        copyEnvToProperty("SHIRE_CLIENT_SECRET", "spring.security.oauth2.client.registration.shire.client-secret");
        SpringApplication.run(PandasNomination.class, args);
    }

    private static void copyEnvToProperty(String env, String property) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) {
            System.setProperty(property, value);
        }
    }
}
