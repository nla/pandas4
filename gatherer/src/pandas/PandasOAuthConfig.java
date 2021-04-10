package pandas;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Loads application-oauth.properties only if OIDC_URL is defined.
 */
@Configuration
@ConditionalOnProperty("OIDC_URL")
@PropertySource("classpath:application-oauth.properties")
public class PandasOAuthConfig {
}
