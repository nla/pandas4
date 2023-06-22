package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import pandas.social.Post;

import java.util.ArrayList;
import java.util.List;

public record TimelineV2(TimelineV2Timeline timeline) {
    public List<TweetV2> tweets() {
        var tweets = new ArrayList<TweetV2>();
        for (var instruction : timeline().instructions()) {
            if (instruction instanceof TimelineAddEntries addEntries) {
                for (var entry: addEntries.entries()) {
                    if (entry.content() instanceof TimelineItem item) {
                        var tweet = item.itemContent().tweet_results().result().tweet();
                        if (tweet != null) tweets.add(tweet);
                    } else if (entry.content() instanceof TimelineModule module) {
                        for (var item: module.items()) {
                            var tweet = item.item().itemContent().tweet_results().result().tweet();
                            if (tweet != null) tweets.add(tweet);
                        }
                    }
                }
            }
        }
        return tweets;
    }

    public record Response(ResponseData data, List<Error> errors) {
        public List<Post> posts() {
            var posts = new ArrayList<Post>();
            var timeline = data().user().result().timeline_v2();
            if (timeline == null) return posts;
            var tweets = timeline.tweets();
            for (var tweet: tweets) {
                posts.add(tweet.toPost());
            }
            return posts;
        }
    }

    public record ResponseData(ResponseUser user) {
    }

    public record ResponseUser(ResponseResult result) {
    }

    public record ResponseResult(String __typename, TimelineV2 timeline_v2) {
    }

    public record SearchResponse(SearchData data) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SearchData(SearchByRawQuery searchByRawQuery) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SearchByRawQuery(TimelineV2 searchTimeline) {
    }

    public record TimelineV2Timeline(List<Instruction> instructions, Object responseObjects, Object metadata) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TimelineClearCache.class, name = "TimelineClearCache"),
            @JsonSubTypes.Type(value = TimelineAddEntries.class, name = "TimelineAddEntries"),
            @JsonSubTypes.Type(value = TimelinePinEntry.class, name = "TimelinePinEntry"),
    })
    public interface Instruction {
    }

    public record TimelineClearCache() implements Instruction {
    }

    public record TimelineAddEntries(List<Entry> entries) implements Instruction {
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // TODO
    public record TimelinePinEntry() implements Instruction {
    }

    public record Entry(String entryId, String sortIndex, Content content) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonSubTypes({
            @JsonSubTypes.Type(TimelineCursor.class),
            @JsonSubTypes.Type(TimelineItem.class),
            @JsonSubTypes.Type(TimelineModule.class)})
    public interface Content {
    }

    @JsonTypeName("TimelineTimelineItem")
    public record TimelineItem(String entryType, TimelineTweet itemContent, Object clientEventInfo) implements Content {
    }

    @JsonTypeName("TimelineTimelineCursor")
    public record TimelineCursor(String entryType, String value, String cursorType,
                                 boolean stopOnEmptyResponse) implements Content {
    }

    @JsonTypeName("TimelineTimelineModule")
    public record TimelineModule(
            String entryType,
            List<ModuleItem> items,
            Object metadata,
            String displayType,
            Object clientEventInfo) implements Content {
    }

    public record ModuleItem(String entryId, ModuleEntry item) {
    }

    public record ModuleEntry(TimelineTweet itemContent, Object clientEventInfo) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonTypeName("TimelineTweet")
    public record TimelineTweet(String itemType, TimelineTweetResults tweet_results, String tweetDisplayType,
                                String ruxContext) {
    }

    public record TimelineTweetResults(TweetOrTombstone result) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonSubTypes({
            @JsonSubTypes.Type(TweetV2.class),
            @JsonSubTypes.Type(TweetTombstone.class),
            @JsonSubTypes.Type(TweetWithVisibilityResults.class)
    })
    public interface TweetOrTombstone {
        TweetV2 tweet();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonTypeName("TweetWithVisibilityResults")
    public record TweetWithVisibilityResults(TweetV2 tweet, Object tweetInterstitial) implements TweetOrTombstone {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", defaultImpl = TweetV2.class)
    @JsonTypeName("Tweet")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TweetV2(
            String restId,
            Core core,
            CardV2 card,
            Object unmentionData,
            Object unifiedCard,
            Object editControl,
            boolean isTranslatable,
            Views views,
            String source,
            TimelineTweetResults quotedStatusResult,
            Tweet legacy
    ) implements TweetOrTombstone {
        public long id() {
            return Long.parseUnsignedLong(restId());
        }

        public Post toPost() {
            Post quotedPost = null;
            if (quotedStatusResult != null && quotedStatusResult.result() instanceof TweetV2 quotedTweet) {
                quotedPost = quotedTweet.toPost();
            }
            var user = core.userResults().result();
            if (user instanceof UserV2 userV2) {
                return legacy.toPost(userV2.legacy(), quotedPost);
            } else {
                throw new RuntimeException("Unexpected user type: " + user.getClass());
            }
        }

        public boolean isNewerThan(Long id) {
            return Long.compareUnsigned(this.id(), id) > 0;
        }

        @Override
        public TweetV2 tweet() {
            return this;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonTypeName("TweetTombstone")
    public record TweetTombstone(Object tombstone) implements TweetOrTombstone {
        @Override
        public TweetV2 tweet() {
            return null;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Core(UserResponse.Results userResults) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CardV2(
            String restId, Object legacy
    ) {
    }

    public record Views(long count, String state) {
    }

    public String nextCursor() {
        for (var instruction : timeline.instructions()) {
            if (instruction instanceof TimelineAddEntries addEntries) {
                for (var entry : addEntries.entries()) {
                    if (entry.content() instanceof TimelineCursor cursor) {
                        if (cursor.cursorType().equals("Bottom")) {
                            return cursor.value();
                        }
                    }
                }
            }
        }
        return null;
    }
}
