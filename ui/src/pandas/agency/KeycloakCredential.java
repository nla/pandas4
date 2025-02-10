package pandas.agency;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KeycloakCredential(
        String id,
        String type,
        String userLabel,
        Long createdDate,
        String secretData,
        String credentialData,
        Integer priority,
        String value,
        Boolean temporary,
        String salt) {

    public KeycloakCredential(String type, String value, Boolean temporary) {
        this(null, type, null, null, null, null, null, value, temporary, null);
    }
}
