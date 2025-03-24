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
import pandas.core.PandasPermissionEvaluator;

import java.net.http.HttpClient;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "userService")
@EnableMethodSecurity
@ConfigurationPropertiesScan("pandas")
public class Pandas {
    public static void main(String[] args) {
        var application = new SpringApplication(Pandas.class);
        if (System.getenv("OIDC_URL") != null || System.getProperty("OIDC_URL") != null) {
            application.setAdditionalProfiles("openid");
        }
        application.run(args);
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
