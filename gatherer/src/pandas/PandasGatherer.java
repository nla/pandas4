package pandas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
public class PandasGatherer {
    public static void main(String[] args) {
        SpringApplication.run(PandasGatherer.class, args);
    }

    @EnableWebFluxSecurity
    static class SecurityConfig {
        @Bean
        public SecurityWebFilterChain securitygWebFilterChain(
                ServerHttpSecurity http) {
            return http.authorizeExchange()
                    .anyExchange().permitAll()
                    .and().csrf().disable().build();
        }
    }
}
