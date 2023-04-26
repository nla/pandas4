package pandas.social.mastodon;

import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.collection.SocialTarget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public class MastodonVisitor {
    private static final Logger log = LoggerFactory.getLogger(MastodonVisitor.class);
    private final String userAgent;
    private final AtomicBoolean stopSignal;

    public MastodonVisitor(String userAgent, AtomicBoolean stopSignal) {
        this.userAgent = userAgent;
        this.stopSignal = stopSignal;
    }

    public void visitTarget(SocialTarget target, WarcWriter warcWriter) throws IOException {
        var client = new MastodonClient(target.getServer(), userAgent, warcWriter);
        if (target.getQuery().startsWith("from:")) {
            var acct = target.getQuery().substring("from:".length());
            visitAccount(target, client, acct);
        } else {
            throw new IllegalArgumentException("Unknown Mastodon query type: " + target.getQuery());
        }
    }

    private void visitAccount(SocialTarget target, MastodonClient client, String acct) throws IOException {
        var account = client.lookupAccount(acct);
        if (account == null) {
            log.warn("Account {} not found on {}", acct, target.getServer());
            return;
        }

        // if we were interrupted, first finish the current range
        if (target.getCurrentRangePosition() != null) {
            log.debug("Resuming interrupted crawl of {}", target);
            visitPostsInRange(target, client, account);
        }

        // now look for new records
        log.debug("Looking for new posts from {}", target);
        target.setCurrentRangeEnd(target.getNewestPostId());
        visitPostsInRange(target, client, account);
    }

    private void visitPostsInRange(SocialTarget target,
                                   MastodonClient client,
                                   Account account) throws IOException {
        log.trace("Visiting posts in {} from {} to {}", account.acct(), target.getCurrentRangePosition(),
                target.getCurrentRangeEnd());
        while (!stopSignal.get()) {
            Instant now = Instant.now();
            var statuses = client.getAccountStatuses(account.id(), target.getCurrentRangePosition(), target.getCurrentRangeEnd());
            log.trace("Got posts for {}: {}", account.acct(), statuses);
            for (var status : statuses) {
                target.expandOldestAndNewest(Long.parseUnsignedLong(status.id()), status.createdAt());
            }
            target.setLastVisitedDate(now);
            if (statuses.isEmpty()) break;
            var oldestFetchedPost = statuses.get(statuses.size() - 1);
            target.setCurrentRangePosition(oldestFetchedPost.id());
        }

        target.clearRange();
    }

    public static void main(String[] args) throws IOException {
        var server = args[0];
        var acct = args[1];
        var target = new SocialTarget(server, "from:" + acct, null);
        new MastodonVisitor("test", new AtomicBoolean()).visitTarget(target, new WarcWriter(Files.newOutputStream(Paths.get("/tmp/test.warc"))));
    }
}
