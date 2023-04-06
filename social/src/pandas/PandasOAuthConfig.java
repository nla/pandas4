package pandas;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.ArrayList;

/**
 * Loads application-oauth.properties only if OIDC_URL is defined.
 */
@Configuration
@ConditionalOnProperty("OIDC_URL")
@PropertySource("classpath:application-oauth.properties")
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@Import(OAuth2ClientAutoConfiguration.class)
public class PandasOAuthConfig {

    @Bean
    InMemoryClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        var registrations = new ArrayList<>(
                OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Bean
    AuthorizedClientServiceOAuth2AuthorizedClientManager oauth2ClientManager(ClientRegistrationRepository clientRegistrationRepository) {
        var clientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);
    }
}
