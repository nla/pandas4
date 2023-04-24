package pandas.social.mastodon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// https://docs.joinmastodon.org/entities/PreviewCard/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PreviewCard(
        @NotNull String url,
        @NotNull String title,
        @NotNull String description,
        @NotNull Type type,
        @NotNull String authorName,
        @Nullable String authorUrl,
        @Nullable String providerName,
        @Nullable String providerUrl,
        @Nullable String html,
        @Nullable Long width,
        @Nullable Long height,
        @Nullable String image,
        @Nullable String embedUrl,
        @Nullable String blurhash
) {
    // https://docs.joinmastodon.org/entities/PreviewCard/#type
    public enum Type {
        @JsonProperty("link") LINK,
        @JsonProperty("photo") PHOTO,
        @JsonProperty("video") VIDEO,
        @JsonProperty("rich") RICH
    }
}
