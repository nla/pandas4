package pandas.social.twitter;

import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.collection.SocialTarget;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public class GraphqlVisitor {
    private static final Logger log = LoggerFactory.getLogger(GraphqlVisitor.class);
    private final SessionManager sessionManager;
    private final AtomicBoolean stopSignal;

    public GraphqlVisitor(SessionManager sessionManager, AtomicBoolean stopSignal) {
        this.sessionManager = sessionManager;
        this.stopSignal = stopSignal;
    }

    public void visitTarget(SocialTarget target, WarcWriter warcWriter) throws IOException, InterruptedException {
        var client = new GraphqlClient(sessionManager, warcWriter);
        if (target.getQuery().startsWith("from:")) {
            var screenName = target.getQuery().substring("from:".length());
            visitAccount(target, client, screenName);
        } else {
            throw new IllegalArgumentException("Unknown query type: " + target.getQuery());
        }
    }

    private void visitAccount(SocialTarget target, GraphqlClient client, String screenName) throws IOException, InterruptedException {
        var user = client.userByScreenName(screenName);
        if (user == null) {
            log.warn("Account {} not found on {}", screenName, target.getServer());
            target.setLastVisitedDate(Instant.now());
            return;
        }
        String cursor = null;
        Long sinceId = target.getNewestPostIdLong();
        log.trace("Visiting tweets in {} until id {}", screenName, sinceId);
        TimelineV2.TweetV2 newestTweet = null;
        long novelTweetCount = 0;
        do {
            Instant now = Instant.now();
            var timeline = client.tweetsAndReplies(user.restId(), cursor);
            target.setLastVisitedDate(now);
            if (timeline == null) {
                log.warn("Failed to get tweets for {} (maybe private account)", target);
                break;
            }
            var tweets = timeline.tweets();
            if (tweets.isEmpty()) {
                log.debug("No more tweets for {}", target);
                break;
            }
            log.trace("Got tweets {}", tweets.stream().map(TimelineV2.TweetV2::restId).toList());
            if (newestTweet == null) newestTweet = tweets.get(0);
            var oldestTweet = tweets.get(tweets.size() - 1);
            target.expandOldest(oldestTweet.id(), oldestTweet.legacy().createdAt());
            if (sinceId != null && Long.compareUnsigned(oldestTweet.id(), sinceId) <= 0) {
                log.debug("Reached end of range for {}", target);
                // count just the portion of the range that's novel
                novelTweetCount += tweets.stream()
                        .filter(tweet -> Long.compareUnsigned(tweet.id(), sinceId) > 0)
                        .count();
                break;
            }
            novelTweetCount += tweets.size();
            cursor = timeline.nextCursor();
        } while (cursor != null && !stopSignal.get());

        if (!stopSignal.get() && newestTweet != null) {
            // only save expand newest if we weren't interrupted
            // otherwise we'll end up with gaps since we can't resume mid-way
            target.expandNewest(newestTweet.id(), newestTweet.legacy().createdAt());
            target.incrementPostCount(novelTweetCount);
        }
    }
}
