package pandas.agency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client for the Keycloak Admin REST API.
 *
 * @see <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html">API documentation</a>
 */
@Service
@ConditionalOnProperty("OIDC_ADMIN_URL")
public class KeycloakAdminClient {
    private final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);
    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager oauth2ClientManager;

    public KeycloakAdminClient(@Value("${OIDC_ADMIN_URL}") String baseUrl,
                               @Autowired ClientRegistrationRepository clientRegistrationRepository) {
        this.oauth2ClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInitializer(request -> {
                    request.getHeaders().setBearerAuth(obtainAccessToken());
                    log.debug("{} {}", request.getMethod(), request.getURI());
                }).build();
    }

    private String obtainAccessToken() {
        var request = OAuth2AuthorizeRequest.withClientRegistrationId("kcadmin")
                .principal("anonymous")
                .build();
        var authorizedClient = oauth2ClientManager.authorize(request);
        return authorizedClient.getAccessToken().getTokenValue();
    }

    private record User(
            String id,
            String username,
            String firstName,
            String lastName,
            String email
    ) {
    }

    private User getUserByUsername(String username) {
        // The username= query param seems to be broken in our current version of Keycloak (or with federated accounts)
        // so we have to do a general search= instead and then filter the results for username matches.
        var users = restClient.get().uri("/users?search={username}&exact=true&briefRepresentation=true", username)
                .retrieve()
                .body(new ParameterizedTypeReference<List<User>>() {
                });
        users.removeIf(user -> !user.username().equals(username));
        if (users == null) throw new RuntimeException("Missing response body");
        if (users.isEmpty()) return null;
        if (users.size() > 1) throw new RuntimeException("Duplicate users for username: " + username);
        return users.get(0);
    }

    public void resetPasswordForUsername(String username, String newPassword) {
        var user = getUserByUsername(username);
        if (user == null) throw new RuntimeException("User not found in Keycloak");
        resetPasswordForUserId(user.id(), newPassword);
    }

    private void resetPasswordForUserId(String userId, String password) {
        restClient.put().uri("/users/{user-id}/reset-password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Credential("password", password, false))
                .retrieve()
                .toBodilessEntity();
    }

    private record Credential(String type, String value, Boolean temporary) {
    }
}