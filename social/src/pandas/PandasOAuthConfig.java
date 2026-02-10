package pandas;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// OAuth2ClientProperties has been removed in Spring Boot 4
// import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Loads application-oauth.properties only if OIDC_URL is defined.
 */
@Configuration
@ConditionalOnProperty("OIDC_URL")
@PropertySource("classpath:application-oauth.properties")
// @EnableConfigurationProperties(OAuth2ClientProperties.class)
//@Import(OAuth2ClientAutoConfiguration.class)
public class PandasOAuthConfig {

    // For non-webapp mode. Spring Boot autoconfigures this in webapp mode.
// FIXME: broken in spring boot 3.4.0, we're not currently this so just commenting out for now
//
//    @Bean
//    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
//    InMemoryClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
//        var registrations = new ArrayList<>(
//                OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values());
//        return new InMemoryClientRegistrationRepository(registrations);
//    }

    // For non-webapp mode. Spring Boot autoconfigures this in webapp mode.
    @Bean
    @ConditionalOnMissingBean(OAuth2AuthorizedClientManager.class)
    AuthorizedClientServiceOAuth2AuthorizedClientManager oauth2ClientManager(ClientRegistrationRepository clientRegistrationRepository) {
        var clientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);
    }
}
