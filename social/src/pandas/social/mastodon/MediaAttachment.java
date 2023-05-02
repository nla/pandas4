package pandas.social.mastodon;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pandas.social.Attachment;

import java.util.ArrayList;
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
        @Nullable Map<String, Meta> meta,
        @Nullable String description,
        @Nullable String blurhash
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Meta(
            Integer width,
            Integer height,
            String frameRate,
            Double duration,
            Long bitrate
    ) {
    }
    public Attachment toGenericMediaAttachment() {
        var sources = new ArrayList<Attachment.Source>();
        Meta originalMeta = meta().get("original");
        if (originalMeta != null) {
            sources.add(new Attachment.Source(url(), null, originalMeta.bitrate(), originalMeta.width(),
                    originalMeta.height()));
        }
        return new Attachment(url, type, description, sources);
    }
}
