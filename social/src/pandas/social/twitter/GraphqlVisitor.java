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
            return;
        }
        String cursor = null;
        Long maxId = target.getNewestPostIdLong();
        log.trace("Visiting tweets in {} until id {}", screenName, maxId);
        do {
            Instant now = Instant.now();
            var timeline = client.tweetsAndReplies(user.restId(), cursor);
            var tweets = timeline.tweets();
            target.setLastVisitedDate(now);
            if (tweets.isEmpty()) {
                log.debug("No more tweets for {}", target);
            }
            for (var tweet : tweets) {
                target.expandOldestAndNewest(tweet.legacy().id(), tweet.legacy().createdAt());
            }
            log.trace("Got tweets {}", tweets.stream().map(t -> t.legacy().id()).toList());
            if (maxId != null && tweets.get(tweets.size() - 1).legacy().id() >= maxId) {
                log.debug("Reached end of range for {}", target);
                break;
            }
            cursor = timeline.nextCursor();
        } while (cursor != null && !stopSignal.get());
    }
}
