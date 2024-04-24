package pandas.agency;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pandas.core.Privileges;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final boolean saveUsersToKeycloak;

    public KeycloakAdminClient(@Value("${OIDC_ADMIN_URL}") String baseUrl,
                               @Value("${SAVE_USERS_TO_KEYCLOAK:false}") boolean saveUsersToKeycloak,
                               @Autowired ClientRegistrationRepository clientRegistrationRepository) {
        this.oauth2ClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInitializer(request -> {
                    request.getHeaders().setBearerAuth(obtainAccessToken());
                    log.debug("{} {}", request.getMethod(), request.getURI());
                }).build();
        this.saveUsersToKeycloak = saveUsersToKeycloak;
    }

    private String obtainAccessToken() {
        var request = OAuth2AuthorizeRequest.withClientRegistrationId("kcadmin")
                .principal("anonymous")
                .build();
        var authorizedClient = oauth2ClientManager.authorize(request);
        return authorizedClient.getAccessToken().getTokenValue();
    }

    private record KeycloakUser(
            String id,
            String username,
            String firstName,
            String lastName,
            String email,
            Map<String, String> attributes
            ) {
        public static KeycloakUser from(User user) {
            Map<String, String> attributes = Map.of("agencyId", Long.toString(user.getAgency().getId()),
                    "accessLevel", user.getRole().getType());
            return new KeycloakUser(null,
                    user.getUserid(),
                    user.getNameGiven(),
                    user.getNameFamily(),
                    user.getEmail(),
                    attributes);
        }
    }

    private KeycloakUser getUserByUsername(String username) {
        // The username= query param seems to be broken in our current version of Keycloak (or with federated accounts)
        // so we have to do a general search= instead and then filter the results for username matches.
        var users = restClient.get().uri("/users?search={username}&exact=true&briefRepresentation=true", username)
                .retrieve()
                .body(new ParameterizedTypeReference<List<KeycloakUser>>() {
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

    public void saveUser(User user) {
        if (!saveUsersToKeycloak) {
            // legacy: reset password only until we unlink the pandas keycloak plugin
            if (user.getPassword() != null) {
                resetPasswordForUsername(user.getUserid(), user.getPassword());
            }
            return;
        }

        var updatedUser = KeycloakUser.from(user);
        var existingUser = getUserByUsername(user.getUserid());
        String userId;
        if (existingUser == null) {
            userId = createUser(updatedUser);
        } else {
            userId = existingUser.id();
            updateUser(userId, updatedUser);
        }

        // reset password if needed
        if (user.getPassword() != null) {
            resetPasswordForUserId(userId, user.getPassword());
        }

        setUserRole(userId, user.getRole().getType().toLowerCase(Locale.ROOT));
    }

    private void setUserRole(String userId, String desiredRole) {
        var existingRoles = getUserRealmRoleMappings(userId);
        boolean userAlreadyHasDesiredRole = false;

        // check if we already have the desired role and remove any others
        for (Role role: existingRoles) {
            if (role.name().equals(desiredRole)) {
                userAlreadyHasDesiredRole = true;
            } else if (Privileges.ROLE_NAMES.contains(role.name())) {
                log.info("Removing realm role {} from user {}", role.name, userId);
                deleteUserRealmRoleMapping(userId, role);
            }
        }

        if (userAlreadyHasDesiredRole) {
            log.debug("User {} already has desired role {}", userId, desiredRole);
            return;
        }

        var availableRoles = getUserAvailableRealmRoleMappings(userId);
        for (Role role: availableRoles) {
            if (role.name().equals(desiredRole)) {
                log.info("Adding realm role {} to user {}", role.name, userId);
                addUserRealmRoleMappings(userId, List.of(role));
                return;
            }
        }
        log.warn("Role {} not in available role list for user {} in Keycloak realm, trying to create it",
                desiredRole, userId);
        String roleId = createRealmRole(desiredRole);
        addUserRealmRoleMappings(userId, List.of(new Role(roleId, desiredRole)));
    }

    /**
     * @return id of created user
     */
    private String createUser(KeycloakUser user) {
        var response = restClient.post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .toBodilessEntity();
        return idFromLocation(response);
    }

    private static @NotNull String idFromLocation(ResponseEntity<Void> response) {
        String path = response.getHeaders().getLocation().getPath();
        int slash = path.lastIndexOf('/');
        if (slash < 0) throw new RuntimeException("Expected / in location: " + response.getHeaders().getLocation());
        return path.substring(slash + 1);
    }

    private void updateUser(String userId, KeycloakUser user) {
        restClient.put().uri("/users/{user-id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .toBodilessEntity();
    }

    private record Role (String id,
                         String name) {
    }

    private List<Role> getUserRealmRoleMappings(String userId) {
        return restClient.get().uri("/users/{user-id}/role-mappings/realm", userId)
                .retrieve().body(new ParameterizedTypeReference<>() {
                });
    }

    private List<Role> getUserAvailableRealmRoleMappings(String userId) {
        return restClient.get().uri("/users/{user-id}/role-mappings/realm/available", userId)
                .retrieve().body(new ParameterizedTypeReference<>() {
                });
    }

    private void addUserRealmRoleMappings(String userId, List<Role> roles) {
        restClient.post().uri("/users/{user-id}/role-mappings/realm", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity();
    }

    private void deleteUserRealmRoleMapping(String userId, Role role) {
        restClient.method(HttpMethod.DELETE).uri("/users/{user-id}/role-mappings/realm", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(role)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * @return id of created role
     */
    private String createRealmRole(String name) {
        return idFromLocation(
        restClient.post().uri("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Role(null, name))
                .retrieve()
                .toBodilessEntity());
    }
}