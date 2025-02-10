package pandas.agency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "OIDC_CREDENTIALS_IMPORT_FILE")
public class KeycloakCredentialImporter implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(KeycloakCredentialImporter.class);
    private final KeycloakAdminClient keycloakAdminClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path importFile;

    public KeycloakCredentialImporter(KeycloakAdminClient keycloakAdminClient, UserRepository userRepository,
                                      @Value("${OIDC_CREDENTIALS_IMPORT_FILE}") Path importFile) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.userRepository = userRepository;
        this.importFile = importFile;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Importing keycloak credentials from {}", importFile);
        try {
            var credentialsByIndividualId = objectMapper.readValue(importFile.toFile(), new TypeReference<Map<Long,List<KeycloakCredential>>>() {
            });
            log.info("Read {} credentials from file", credentialsByIndividualId.size());

            // Get corresponding users from UserRepository
            Iterable<User> users = userRepository.findAllById(credentialsByIndividualId.keySet());

            // Save each user back to Keycloak with the credentials
            var seenUserIds = new HashSet<Long>();
            for (var user : users) {
                seenUserIds.add(user.getId());
                log.info("Importing credentials for user {}", user.getUserid());
                List<KeycloakCredential> credentials = credentialsByIndividualId.get(user.getId());
                if (credentials == null) throw new IllegalStateException("No credentials found for user " + user.getUserid());
                keycloakAdminClient.saveUser(user, credentials);
            }

            // Check for credentials without corresponding users
            var orphanedIds = credentialsByIndividualId.keySet().stream()
                    .filter(id -> !seenUserIds.contains(id))
                    .collect(Collectors.toSet());
            if (!orphanedIds.isEmpty()) {
                log.warn("No user found for credentials with individualIds: {}", orphanedIds);
            }
        } catch (Exception e) {
            log.error("Error importing keycloak credentials", e);
        }
    }
}
