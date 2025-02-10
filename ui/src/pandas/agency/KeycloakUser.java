package pandas.agency;

import java.util.Map;
import java.util.List;

record KeycloakUser(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        Map<String, String> attributes,
        List<KeycloakCredential> credentials
) {
    public static KeycloakUser from(User user, List<KeycloakCredential> credentials) {
        Map<String, String> attributes = Map.of("agencyId", Long.toString(user.getAgency().getId()),
                "accessLevel", user.getRole().getType());
        return new KeycloakUser(null,
                user.getUserid(),
                user.getNameGiven(),
                user.getNameFamily(),
                user.getEmail(),
                attributes,
                credentials);
    }
}
