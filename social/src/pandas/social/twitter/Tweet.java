package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;
import pandas.social.Attachment;
import pandas.social.Post;
import pandas.social.Site;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/tweet
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
public record Tweet(
        @NotNull @JsonFormat(pattern = "EEE MMM dd HH:mm:ss Z yyyy", locale = "ENGLISH") Instant createdAt,
        long id,
        @NotNull String fullText,
        @NotNull String userIdStr,
        TEntities entities,
        ExtendedEntities extendedEntities,
        long replyCount,
        long retweetCount,
        long quoteCount,
        long favoriteCount,
        String inReplyToScreenName
) {
    // https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/entities
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(SnakeCaseStrategy.class)
    public record TEntities(
            List<Mention> userMentions,
            List<Hashtag> hashtags,
            List<Url> urls
    ) {
    }

    public interface Entity {
        int[] indices();
    }

    // https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/entities#hashtags
    @JsonNaming(SnakeCaseStrategy.class)
    public record Hashtag(
            @NotNull String text,
            int @NotNull [] indices
    ) implements Entity {
    }

    // https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/entities#mentions
    @JsonNaming(SnakeCaseStrategy.class)
    public record Mention(
            long id,
            @NotNull String idStr,
            @NotNull String name,
            @NotNull String screenName,
            int @NotNull [] indices
    ) implements Entity {
    }

    // https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/entities#urls
    @JsonNaming(SnakeCaseStrategy.class)
    public record Url(
            @NotNull String url,
            @NotNull String displayUrl,
            @NotNull String expandedUrl,
            int @NotNull [] indices
    ) implements Entity {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(SnakeCaseStrategy.class)
    public record ExtendedEntities(List<Media> media) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonNaming(SnakeCaseStrategy.class)
        public record Media(
                @NotNull URI mediaUrlHttps,
                @NotNull String type,
                String extAltText) {
            public Attachment toGenericMediaAttachment() {
                return new Attachment(mediaUrlHttps, type, extAltText);
            }
        }
    }


    public Post toGenericPost(Map<String, User> users) {
        StringBuilder builder = new StringBuilder();
        int position = 0;
        for (var url : entities.urls()) {
            builder.append(Entities.escape(fullText.substring(position, url.indices()[0])));
            builder.append("<a href=\"").append(Entities.escape(url.expandedUrl())).append("\">")
                    .append(Entities.escape(url.displayUrl()))
                    .append("</a>");
            position = url.indices()[1];
        }
        builder.append(Entities.escape(fullText.substring(position)));

        String htmlContent = builder.toString();

        htmlContent = Jsoup.clean(htmlContent, "", Safelist.basic(), new Document.OutputSettings().prettyPrint(false));

        List<Attachment> attachments = Collections.emptyList();
        if (extendedEntities != null) {
            attachments = extendedEntities.media.stream()
                    .map(ExtendedEntities.Media::toGenericMediaAttachment)
                    .toList();
        }

        User user = users.get(userIdStr);
        return new Post(
                "https://twitter.com/" + user.screenName() + "/status/" + id,
                createdAt,
                user.toGenericAccount(),
                new Site("Twitter"),
                inReplyToScreenName,
                entities.userMentions.stream().map(Mention::screenName).toList(),
                htmlContent,
                attachments,
                replyCount,
                retweetCount,
                quoteCount,
                favoriteCount
        );
    }
}
