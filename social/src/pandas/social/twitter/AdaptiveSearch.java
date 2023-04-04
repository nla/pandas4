package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdaptiveSearch(GlobalObjects globalObjects, Timeline timeline) {
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.class);

    public String nextCursor() {
        if (globalObjects().tweets().isEmpty()) {
            return null; // reached the end
        }
        for (var entry : timeline().allEntries()) {
            if (entry.entryId().equals("sq-cursor-bottom")) {
                if (entry.content() instanceof Timeline.Operation operation) {
                    return operation.cursor().value();
                } else {
                    throw new RuntimeException("Unexpected content for sq-cursor-bottom: " + entry.content().getClass());
                }
            }
        }
        throw new RuntimeException("Couldn't find sq-cursor-bottom in timeline");
    }

    public List<Tweet> tweets() {
        List<Tweet> tweets = new ArrayList<>();
        for (var entry : timeline().allEntries()) {
            if (entry.content() instanceof Timeline.Item item) {
                if (item.content() instanceof Timeline.TweetRef tweetRef) {
                    var tweet = globalObjects().tweets().get(tweetRef.id());
                    if (tweet != null) {
                        tweets.add(tweet);
                    } else {
                        log.warn("Tweet {} not found in global objects", tweetRef.id());
                    }
                } else {
                    log.warn("Unexpected item content: {}", item.content());
                }
            } else if (!(entry.content() instanceof Timeline.Operation)) {
                log.warn("Unexpected entry content: {}", entry.content());
            }
        }
        return tweets;
    }

    public List<Post> posts() {
        return tweets().stream()
                .map(tweet -> tweet.toGenericPost(globalObjects().users()))
                .toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GlobalObjects(
            Map<String, Tweet> tweets,
            Map<String, User> users) {
    }
}
