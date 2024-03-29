package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import pandas.social.Account;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
public record User(
        long id,
        @NotNull String name,
        @NotNull String screenName,
        @NotNull String profileImageUrlHttps,
        String profileBannerUrl
) {
    public Account toGenericAccount() {
        return new Account(screenName, name, profileImageUrlHttps, profileBannerUrl);
    }
}
