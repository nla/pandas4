package pandas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@EnableJpaAuditing
public class PandasSocial {
    public static void main(String[] args) {
        SpringApplication.run(PandasSocial.class, args);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests(r -> r.anyRequest().permitAll());
        http.oauth2Login(Customizer.withDefaults());
        http.oauth2Client();
        return http.build();
    }
}
