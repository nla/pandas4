package pandas.social.mastodon;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pandas.social.Attachment;

import java.util.ArrayList;

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
        @Nullable Meta meta,
        @Nullable String description,
        @Nullable String blurhash
) {

    record Meta(
            MetaValue original,
            MetaValue small,
            Point focus
    ) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ImageGeometry.class),
            @JsonSubTypes.Type(value = VideoMetadata.class)
    })
    interface MetaValue {
        Integer width();

        Integer height();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ImageGeometry(
            Integer width,
            Integer height,
            String size,
            Double aspect) implements MetaValue {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record VideoMetadata(
            Integer width,
            Integer height,
            String frameRate,
            Double duration,
            Long bitrate) implements MetaValue {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Point(Double x, Double y) {
    }

    public Attachment toGenericMediaAttachment() {
        var sources = new ArrayList<Attachment.Source>();
        MetaValue originalMeta = meta().original();
        if (originalMeta != null) {
            sources.add(new Attachment.Source(url(),
                    null,
                    originalMeta instanceof VideoMetadata video ? video.bitrate() : null,
                    originalMeta.width(),
                    originalMeta.height()));
        }
        return new Attachment(url, type, description, sources);
    }
}
