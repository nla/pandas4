package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public record UserResponse(UserResponse.ResponseData data) {

    record ResponseData(Results user) {
    }

    record Results(UserOrError result) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonTypeName("UserUnavailable")
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UserUnavailable(String reason, Object unavailableMessage) implements UserOrError {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = UserV2.class),
            @JsonSubTypes.Type(value = UserUnavailable.class),
    })
    public interface UserOrError {
    }
}
