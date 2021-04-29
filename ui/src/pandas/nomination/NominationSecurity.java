package pandas.nomination;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(99)
public class NominationSecurity extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/nominate")
                .authorizeRequests()
                .anyRequest().authenticated()
                .and().oauth2Login(o2 -> o2.loginPage("/oauth2/authorization/shire"));
    }
}
