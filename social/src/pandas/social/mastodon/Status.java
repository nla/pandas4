package pandas.social.mastodon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pandas.social.Post;
import pandas.social.Site;

import java.time.Instant;
import java.util.List;

// https://docs.joinmastodon.org/entities/Status/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Status(
        @NotNull String id,
        @NotNull String uri,
        @NotNull Instant createdAt,
        @NotNull Account account,
        @NotNull String content,
        @NotNull Visibility visibility,
        boolean sensitive,
        @NotNull String spoilerText,
        @NotNull List<MediaAttachment> mediaAttachments,
        @Nullable Application application,
        @NotNull List<Mention> mentions,
        @NotNull List<Tag> tags,
        @NotNull List<CustomEmoji> emojis,
        long reblogsCount,
        long favouritesCount,
        long repliesCount,
        @Nullable String url,
        @Nullable String inReplyToId,
        @Nullable String inReplyToAccountId,
        @Nullable Status reblog,
        @Nullable Poll poll,
        @Nullable PreviewCard card,
        @Nullable String language,
        @Nullable String text,
        @Nullable Instant editedAt) {

    // https://docs.joinmastodon.org/entities/Status/#application
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Application(
            @NotNull String name,
            @Nullable String website
    ) {
    }

    // https://docs.joinmastodon.org/entities/Status/#Mention
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Mention(
            @NotNull String id,
            @NotNull String username,
            @NotNull String url,
            @NotNull String acct
    ) {
    }

    // https://docs.joinmastodon.org/entities/Status/#Tag
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Tag(
            @NotNull String name,
            @NotNull String url
    ) {
    }

    // https://docs.joinmastodon.org/entities/Status/#visibility
    public enum Visibility {
        @JsonProperty("public") PUBLIC,
        @JsonProperty("unlisted") UNLISTED,
        @JsonProperty("private") PRIVATE,
        @JsonProperty("direct") DIRECT
    }

    public Post toGenericPost(Site site) {
        return new Post(
                url,
                createdAt,
                account.toGenericAccount(),
                site,
                inReplyToAccountId,
                mentions().stream().map(Mention::username).toList(),
                content,
                mediaAttachments.stream().map(MediaAttachment::toGenericMediaAttachment).toList(),
                repliesCount,
                reblogsCount,
                null,
                favouritesCount
        );
    }
}
