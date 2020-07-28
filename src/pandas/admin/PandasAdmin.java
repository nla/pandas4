package pandas.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PandasAdmin {
    public static void main(String[] args) {
        copyEnvToProperty("OIDC_URL", "spring.security.oauth2.client.provider.oidc.issuer-uri");
        copyEnvToProperty("OIDC_CLIENT_ID", "spring.security.oauth2.client.registration.oidc.client-id");
        copyEnvToProperty("OIDC_CLIENT_SECRET", "spring.security.oauth2.client.registration.oidc.client-secret");
        SpringApplication.run(PandasAdmin.class, args);
    }

    private static void copyEnvToProperty(String env, String property) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) {
            System.setProperty(property, value);
        }
    }
}
