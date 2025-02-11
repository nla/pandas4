package pandas.agency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class KeycloakCredentialDumper {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String keycloakRealmId = System.getenv("KEYCLOAK_REALM_ID");
    private final String keycloakDbUrl = System.getenv("KEYCLOAK_DB_URL");
    private final String keycloakDbUser = System.getenv("KEYCLOAK_DB_USER");
    private final String keycloakDbPassword = System.getenv("KEYCLOAK_DB_PASSWORD");

    public static void main(String[] args) {
        new KeycloakCredentialDumper().run();
    }

    private void run() {
        try (Connection connection = DriverManager.getConnection(keycloakDbUrl, keycloakDbUser, keycloakDbPassword)) {
            try (var stmt = connection.prepareStatement("SELECT * FROM FED_USER_CREDENTIAL where REALM_ID = ?")) {
                stmt.setString(1, keycloakRealmId);
                try (var resultSet = stmt.executeQuery()) {
                    var credentials = new TreeMap<Long, List<KeycloakCredential>>();
                    long count = 0;
                    while (resultSet.next()) {
                        KeycloakCredential credential = new KeycloakCredential(
                                resultSet.getString("ID"),
                                resultSet.getString("TYPE").toLowerCase(),
                                resultSet.getString("USER_LABEL"),
                                resultSet.getObject("CREATED_DATE", Long.class),
                                resultSet.getString("SECRET_DATA"),
                                resultSet.getString("CREDENTIAL_DATA"),
                                resultSet.getObject("PRIORITY", Integer.class),
                                null, // value
                                null, // temporary
                                resultSet.getString("SALT")
                        );
                        long individualId = extractIndividualIdFromUserId(resultSet.getString("USER_ID"));
                        credentials.computeIfAbsent(individualId, id -> new ArrayList<>()).add(credential);
                        count++;
                    }
                    var objectMapper = new ObjectMapper();
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                    objectMapper.writeValue(System.out, credentials);
                    log.info("Dumped {} credentials for {} users", count, credentials.size());
                }
            }
        } catch (SQLException e) {
            log.error("Failed to connect to MySQL", e);
        } catch (IOException e) {
            log.error("IO error", e);
        }
    }

    /**
     * "f:e7b8cb8e-4890-4702-b486-6cd1b2b3d6a7:12345" -> 12345
     */
    private static long extractIndividualIdFromUserId(String userId) {
        if (!userId.startsWith("f:")) throw new IllegalArgumentException("Not a federated user id: " + userId);
        return Long.parseLong(userId.split(":")[2]);
    }
}
