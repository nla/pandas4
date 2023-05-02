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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/tweet
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
public record Tweet(
        @NotNull @JsonFormat(pattern = "EEE MMM dd HH:mm:ss Z yyyy", locale = "ENGLISH") Instant createdAt,
        long id,
        String idStr,
        @NotNull String fullText,
        @NotNull String userIdStr,
        TEntities entities,
        ExtendedEntities extendedEntities,
        long replyCount,
        long retweetCount,
        long quoteCount,
        long favoriteCount,
        String inReplyToScreenName,
        TimelineV2.TimelineTweetResults retweetedStatusResult
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
                @NotNull String mediaUrlHttps,
                @NotNull String type,
                String extAltText,
                VideoInfo videoInfo,
                Map<String, Size> sizes,
                OriginalInfo originalInfo) {

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonNaming(SnakeCaseStrategy.class)
            public record VideoInfo(int[] aspectRatio, List<Variant> variants) {
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonNaming(SnakeCaseStrategy.class)
            public record Variant(Long bitrate, String contentType, String url) {
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonNaming(SnakeCaseStrategy.class)
            public record Size(int w, int h, String resize) {
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonNaming(SnakeCaseStrategy.class)
            public record OriginalInfo(int width, int height) {
            }

            static final Pattern VIDEO_SIZE_RE = Pattern.compile("https://video\\.twimg\\.com/ext_tw_video/[0-9]+/pu/vid/([0-9]+)x([0-9]+)/[^/]+");

            record ExtensionInfo(String base, String extension, String contentType) {
                private static final Pattern PATTERN = Pattern.compile("(.*/[^/]*)\\.([a-z0-9A-Z]+)");
                private static final Map<String,String> CONTENT_TYPES = Map.of(
                        "gif", "image/gif",
                        "jpg", "image/jpeg",
                        "mp4", "video/mp4",
                        "png", "image/png");

                /**
                 * "foo/bar.jpg" -> ("foo/bar", "jpg", "image/jpeg")
                 */
                public static ExtensionInfo of(String url) {
                    var matcher = PATTERN.matcher(url);
                    if (matcher.matches()) {
                        String extension = matcher.group(2);
                        return new ExtensionInfo(matcher.group(1), extension, CONTENT_TYPES.get(extension));
                    }
                    return new ExtensionInfo(url, null, null);
                }
            }

            public Attachment toGenericMediaAttachment() {
                var urlExt = ExtensionInfo.of(mediaUrlHttps);
                var sources = new ArrayList<Attachment.Source>();

                if (sizes != null && "photo".equals(type) && urlExt.contentType() != null) {
                    boolean seenOriginalSize = false;
                    for (var entry: sizes.entrySet()) {
                        var size = entry.getValue();
                        sources.add(new Attachment.Source(urlExt.base() + "?format=" + urlExt.extension() + "&name=" + entry.getKey(),
                                urlExt.contentType(), null, size.w, size.h));
                        if (originalInfo != null && size.w >= originalInfo.width && size.h >= originalInfo.height) {
                            seenOriginalSize = true;
                        }
                    }

                    if (originalInfo != null && !seenOriginalSize) {
                        int w, h;
                        if (originalInfo.width > originalInfo.height) {
                            w = Integer.min(originalInfo.width, 4096);
                            h = (int) Math.round((double) w / originalInfo.width * originalInfo.height);
                        } else {
                            h = Integer.min(originalInfo.height, 4096);
                            w = (int) Math.round((double) h / originalInfo.height * originalInfo.width);
                        }
                        sources.add(0, new Attachment.Source(urlExt.base() + "?format=" + urlExt.extension() + "&name=4096x4096",
                                urlExt.contentType(), null, w, h));
                    }
                }

                if (videoInfo != null && videoInfo.variants != null) {
                    for (var variant : videoInfo.variants) {
                        Integer width = null;
                        Integer height = null;
                        var matcher = VIDEO_SIZE_RE.matcher(variant.url);
                        if (matcher.matches()) {
                            width = Integer.parseInt(matcher.group(1));
                            height = Integer.parseInt(matcher.group(2));
                        }
                        sources.add(new Attachment.Source(variant.url, variant.contentType, variant.bitrate,
                                width, height));
                    }
                }
                return new Attachment(mediaUrlHttps, type, extAltText, sources);
            }
        }

    }

    public Post toPost(Map<String, User> users) {
        User user = users.get(userIdStr);
        return toPost(user, null);
    }

    Post toPost(User user, Post quotedPost) {
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

        Post repost = null;
        if (retweetedStatusResult != null && retweetedStatusResult.result() instanceof TimelineV2.TweetV2 retweetV2) {
            repost = retweetV2.toPost();
        }

        return new Post(
                "https://twitter.com/" + user.screenName() + "/status/" + idStr(),
                createdAt,
                user.toGenericAccount(),
                new Site("Twitter"),
                inReplyToScreenName,
                entities.userMentions.stream().map(Mention::screenName).toList(),
                htmlContent,
                attachments,
                repost,
                quotedPost,
                replyCount,
                retweetCount,
                quoteCount,
                favoriteCount
        );
    }
}
