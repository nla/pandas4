package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

public record TimelineV2(TimelineV2Timeline timeline) {
    public List<TweetV2> tweets() {
        var tweets = new ArrayList<TweetV2>();
        for (var instruction : timeline().instructions()) {
            if (instruction instanceof TimelineAddEntries addEntries) {
                for (var entry: addEntries.entries()) {
                    if (entry.content() instanceof TimelineItem item) {
                        tweets.add(item.itemContent().tweet_results().result());
                    }
                }
            }
        }
        return tweets;
    }

    public record Response(ResponseData data) {
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

    public record TimelineV2Timeline(List<Instruction> instructions, Object responseObjects) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TimelineClearCache.class, name = "TimelineClearCache"),
            @JsonSubTypes.Type(value = TimelineAddEntries.class, name = "TimelineAddEntries")})
    public interface Instruction {
    }

    public record TimelineClearCache() implements Instruction {
    }

    public record TimelineAddEntries(List<Entry> entries) implements Instruction {
    }

    public record Entry(String entryId, String sortIndex, Content content) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonSubTypes({
            @JsonSubTypes.Type(TimelineItem.class),
            @JsonSubTypes.Type(TimelineCursor.class)})
    public interface Content {
    }

    @JsonTypeName("TimelineTimelineItem")
    public record TimelineItem(String entryType, TimelineTweet itemContent, Object clientEventInfo) implements Content {
    }

    @JsonTypeName("TimelineTimelineCursor")
    public record TimelineCursor(String entryType, String value, String cursorType,
                                 boolean stopOnEmptyResponse) implements Content {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonTypeName("TimelineTweet")
    public record TimelineTweet(String itemType, TimelineTweetResults tweet_results, String tweetDisplayType,
                                String ruxContext) {
    }

    public record TimelineTweetResults(TweetV2 result) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
    @JsonTypeName("Tweet")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    ) {
        public long id() {
            return Long.parseUnsignedLong(restId());
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Core(UserV2.Results userResults) {
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
