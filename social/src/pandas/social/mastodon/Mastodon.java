package pandas.social.mastodon;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pandas.social.Attachment;
import pandas.social.Post;
import pandas.social.Site;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

public class Mastodon {
    // https://docs.joinmastodon.org/entities/Account/
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
        public record Field(
                @NotNull String name,
                @NotNull String value,
                @Nullable Instant verifiedAt
        ) {
        }
    }

    // https://docs.joinmastodon.org/entities/Status/
    public record Status(
            @NotNull String id,
            @NotNull String uri,
            @NotNull Instant createdAt,
            @NotNull Account account,
            @NotNull String content,
            @NotNull Status.Visibility visibility,
            boolean sensitive,
            @NotNull String spoilerText,
            @NotNull List<MediaAttachment> mediaAttachments,
            @Nullable Status.Application application,
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
        public record Application(
                @NotNull String name,
                @Nullable String website
        ) {
        }

        // https://docs.joinmastodon.org/entities/Status/#Mention
        public record Mention(
                @NotNull String id,
                @NotNull String username,
                @NotNull String url,
                @NotNull String acct
        ) {
        }

        // https://docs.joinmastodon.org/entities/Status/#Tag
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


    // https://docs.joinmastodon.org/entities/CustomEmoji/
    public record CustomEmoji() {
    }

    // https://docs.joinmastodon.org/entities/MediaAttachment/
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

    // https://docs.joinmastodon.org/entities/Poll/
    public record Poll(
            @NotNull String id,
            @NotNull String expiresAt,
            @Nullable Instant expired,
            boolean multiple,
            @NotNull List<Option> options,
            @NotNull List<Integer> votes
    ) {
        // https://docs.joinmastodon.org/entities/Poll/#Option
        public record Option(
                @NotNull String title,
                @Nullable Long votes
        ) {
        }
    }

    // https://docs.joinmastodon.org/entities/PreviewCard/
    public record PreviewCard(
            @NotNull String url,
            @NotNull String title,
            @NotNull String description,
            @NotNull PreviewCard.Type type,
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
}
