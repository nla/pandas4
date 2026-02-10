package pandas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import pandas.core.PandasBanner;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class PandasGatherer {
    public static void main(String[] args) {
        var app = new SpringApplication(PandasGatherer.class);
        app.setBanner(new PandasBanner());
        app.run(args);
    }

    @EnableWebFluxSecurity
    static class SecurityConfig {
        @Bean
        public SecurityWebFilterChain securitygWebFilterChain(
                ServerHttpSecurity http) {
            return http.authorizeExchange(exchanges -> exchanges
                    .anyExchange().permitAll())
                    .csrf(csrf -> csrf.disable())
                    .build();
        }
    }
}
