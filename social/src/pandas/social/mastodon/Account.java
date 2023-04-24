package pandas.social.mastodon;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

// https://docs.joinmastodon.org/entities/Account/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Account(
        @NotNull String id,
        @NotNull String username,
        @NotNull String acct,
        @NotNull String url,
        @NotNull String displayName,
        @NotNull String note,
        @NotNull String avatar,
        @NotNull String avatarStatic,
        @NotNull String header,
        @NotNull String headerStatic,
        boolean locked,
        @Nullable List<Field> fields,
        @Nullable List<CustomEmoji> emojis,
        @Nullable List<Object> roles,
        boolean noindex,
        boolean bot,
        boolean group,
        boolean discoverable,
        @Nullable Account moved,
        @JsonInclude(NON_DEFAULT) boolean suspended,
        @JsonInclude(NON_DEFAULT) boolean limited,
        @Nullable Instant createdAt,
        @Nullable LocalDate lastStatusAt,
        long statusesCount,
        long followersCount,
        long followingCount) {

    public pandas.social.Account toGenericAccount() {
        return new pandas.social.Account(
                username,
                displayName,
                avatar
        );
    }

    // https://docs.joinmastodon.org/entities/Account/#Field
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Field(
            @NotNull String name,
            @NotNull String value,
            @Nullable Instant verifiedAt
    ) {
    }
}
