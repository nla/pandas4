package pandas.social.mastodon;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

// https://docs.joinmastodon.org/entities/Poll/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Poll(
        @NotNull String id,
        @NotNull String expiresAt,
        @Nullable Instant expired,
        boolean multiple,
        @NotNull List<Option> options,
        @NotNull List<Integer> votes
) {
    // https://docs.joinmastodon.org/entities/Poll/#Option
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Option(
            @NotNull String title,
            @Nullable Long votes
    ) {
    }
}
