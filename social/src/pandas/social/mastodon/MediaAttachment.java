package pandas.social.mastodon;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pandas.social.Attachment;

import java.util.Map;

// https://docs.joinmastodon.org/entities/MediaAttachment/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MediaAttachment(
        @NotNull String id,
        @NotNull String type,
        @NotNull String url,
        @Nullable String previewUrl,
        @Nullable String previewRemoteUrl,
        @Nullable String remoteUrl,
        @Nullable String textUrl, // deprecated
        @Nullable Map<String, Object> meta,
        @Nullable String description,
        @Nullable String blurhash
) {
    public Attachment toGenericMediaAttachment() {
        return new Attachment(url, type, description);
    }
}
