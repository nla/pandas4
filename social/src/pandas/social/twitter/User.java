package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import pandas.social.Account;

import java.net.URL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
public record User(
        long id,
        @NotNull String name,
        @NotNull String screenName,
        @NotNull URL profileImageUrlHttps
) {
    public Account toGenericAccount() {
        return new Account(screenName, name, profileImageUrlHttps);
    }
}
