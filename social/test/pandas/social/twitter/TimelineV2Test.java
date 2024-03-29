package pandas.social.twitter;

import org.junit.jupiter.api.Test;
import pandas.social.SocialJson;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimelineV2Test {
    @Test
    public void test() throws IOException {
        URL json = Objects.requireNonNull(TimelineV2Test.class.getResource("test-timelinev2.json"));
        var response = SocialJson.mapper.readValue(json, TimelineV2.Response.class);
        TimelineV2 timeline = response.data().user().result().timeline_v2();
        assertEquals("nextcursor", timeline.nextCursor());
        assertEquals(1, timeline.tweets().size());

        var tweet = (TimelineV2.TweetV2)timeline.tweets().get(0);
        var post = tweet.toPost();
        assertEquals("https://twitter.com/somebody/status/1711392519734270000", post.url());
        assertEquals("This is a test", post.content());
        assertEquals("somebody", post.author().username());
    }
}