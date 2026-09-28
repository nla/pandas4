package pandas;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import pandas.core.PandasBanner;
import pandas.core.PandasPermissionEvaluator;

import java.net.http.HttpClient;
import java.util.ArrayList;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "userService")
@EnableMethodSecurity
@ConfigurationPropertiesScan("pandas")
public class Pandas {
    public static void main(String[] args) {
        var application = new SpringApplication(Pandas.class);
        var profiles = new ArrayList<String>();
        if (isSet("OIDC_URL")) profiles.add("openid");
        if (isSet("SMTP_HOST")) profiles.add("mail");
        application.setAdditionalProfiles(profiles.toArray(String[]::new));
        application.setBanner(new PandasBanner());
        application.run(args);
    }

    private static boolean isSet(String name) {
        return System.getenv(name) != null || System.getProperty(name) != null;
    }

    @Bean(name = "htmlSanitizer")
    public PolicyFactory htmlSanitizer() {
        return Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.LINKS).and(Sanitizers.TABLES);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Bean
    static MethodSecurityExpressionHandler expressionHandler(PandasPermissionEvaluator permissionEvaluator) {
        var expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}
