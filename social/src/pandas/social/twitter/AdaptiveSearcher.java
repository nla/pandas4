package pandas.social.twitter;

import org.netpreserve.jwarc.HttpRequest;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.collection.SocialTarget;
import pandas.social.SocialJson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.netpreserve.jwarc.MessageVersion.HTTP_1_0;

public class AdaptiveSearcher {
    private static final Logger log = LoggerFactory.getLogger(AdaptiveSearcher.class);
    private static final String ADAPTIVE_SEARCH_URL = "https://api.twitter.com/2/search/adaptive.json?include_profile_interstitial_type=1&include_blocking=1&include_blocked_by=1&include_followed_by=1&include_want_retweets=1&include_mute_edge=1&include_can_dm=1&include_can_media_tag=1&include_ext_has_nft_avatar=1&include_ext_is_blue_verified=1&include_ext_verified_type=1&skip_status=1&cards_platform=Web-12&include_cards=1&include_ext_alt_text=true&include_ext_limited_action_results=true&include_quote_count=true&include_reply_count=1&tweet_mode=extended&include_ext_collab_control=true&include_ext_views=true&include_entities=true&include_user_entities=true&include_ext_media_color=true&include_ext_media_availability=true&include_ext_sensitive_media_warning=true&include_ext_trusted_friends_metadata=true&send_error_codes=true&simple_quoted_tweet=true&tweet_search_mode=live&count=20&query_source=spelling_expansion_revert_click&pc=1&spelling_corrections=1&ext=mediaStats%2ChighlightedLabel%2ChasNftAvatar%2CvoiceInfo%2Cenrichments%2CsuperFollowMetadata%2CunmentionInfo,editControl,collab_control,vibe&include_ext_edit_control=true";
    public static final int DEFAULT_DELAY_MILLIS = 8000;

    private final String userAgent;
    private final AtomicBoolean stopSignal;
    private int delayMillis = DEFAULT_DELAY_MILLIS;
    private final SessionManager sessionManager;

    public AdaptiveSearcher(SessionManager sessionManager, AtomicBoolean stopSignal) {
        this.userAgent = sessionManager.getUserAgent();
        this.stopSignal = stopSignal;
        this.sessionManager = sessionManager;
        log.trace("AdaptiveSearcher created (userAgent={})", userAgent);
    }

    private void innerSearch(String query, WarcWriter warcWriter, SocialTarget target) throws IOException, InterruptedException {
        int page = 0;
        String cursor = null;
        int tries = 0;
        do {
            log.trace("Searching for '{}' with cursor '{}'", query, cursor);
            String url = ADAPTIVE_SEARCH_URL + "&q=" + URLEncoder.encode(query, UTF_8);
            if (cursor != null) {
                url += "&cursor=" + URLEncoder.encode(cursor, UTF_8);
            }

            long startNanos = System.nanoTime();
            Instant now = Instant.now();
            URI uri = URI.create(url);
            var session = sessionManager.getOrCreateSession();
            var httpRequest = new HttpRequest.Builder("GET", uri.getRawPath() + "?" + uri.getRawQuery())
                    .version(HTTP_1_0)
                    .addHeader("Authorization", "Bearer " + sessionManager.getOrFetchBearerToken())
                    .addHeader("Connection", "close")
                    .addHeader("Cookie", session.cookies())
                    .addHeader("Host", uri.getHost())
                    .addHeader("User-Agent", userAgent)
                    .addHeader("x-guest-token", session.guestToken())
                    .build();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            warcWriter.fetch(uri, httpRequest, buffer);
            var httpResponse = HttpResponse.parse(Channels.newChannel(new ByteArrayInputStream(buffer.toByteArray())));
            if (httpResponse.status() != 200) {
                log.error("Status {} from {}", httpResponse.status(), uri);
                if (tries++ < 5) {
                    long secondsToSleep = ((1L << tries) - 1) * 110 - 80;
                    log.info("Backing off for {} seconds", secondsToSleep);
                    Thread.sleep(secondsToSleep * 1000);
                    sessionManager.invalidateSession();
                    // TODO: expire cursor?
                    continue;
                } else {
                    log.error("Too many retries, stopping archiver");
                    stopSignal.set(true);
                    break;
                }
            }
            if (httpResponse.headers().first("x-rate-limit-remaining").map(Integer::parseInt).orElse(10) < 10) {
                sessionManager.invalidateSession();
            }
            var adaptiveResponse = SocialJson.mapper.readValue(httpResponse.body().stream(), AdaptiveSearch.class);
            var tweets = adaptiveResponse.tweets();
            if (log.isTraceEnabled()) {
                log.trace("Got tweets {}", tweets.stream().map(Tweet::id).toList());
            }
            target.setLastVisitedDate(now);
            if (!tweets.isEmpty()) {
                target.incrementPostCount(tweets.size());
                if (page == 0) {
                    var newestFetchedTweet = tweets.get(0);
                    target.expandOldestAndNewest(newestFetchedTweet.id(), newestFetchedTweet.createdAt());
                }
                var oldestFetchedTweet = tweets.get(tweets.size() - 1);
                target.expandOldestAndNewest(oldestFetchedTweet.id(), oldestFetchedTweet.createdAt());
                target.setCurrentRangePositionLong(oldestFetchedTweet.id());
            }
            cursor = adaptiveResponse.nextCursor();
            if (stopSignal.get()) return;
            page++;
            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            long millisToSleep = delayMillis - elapsedMillis;
            if (millisToSleep > 0) {
                log.trace("Sleeping for {}ms", millisToSleep);
                Thread.sleep(millisToSleep);
            }
            if (stopSignal.get()) return;
        } while (cursor != null);

        // we've finished the current range, so clear it
        target.clearRange();
    }

    public void fetchRange(String query, Long start, Long end, WarcWriter warcWriter, SocialTarget target) throws IOException, InterruptedException {
        target.setCurrentRangeEndLong(end);

        if (start != null) query += " max_id:" + start;
        if (end != null) query += " since_id:" + end;
        innerSearch(query, warcWriter, target);
    }

    public void search(String query, WarcWriter warcWriter, SocialTarget target) throws IOException, InterruptedException {
        // if we were interrupted, first finish the current range
        if (target.getCurrentRangePosition() != null) {
            log.info("Resuming interrupted search for '{}' from {} to {}", query,
                    target.getCurrentRangePosition(), target.getCurrentRangeEnd());
            fetchRange(query, target.getCurrentRangePositionLong() + 1,
                    target.getCurrentRangeEndLong(), warcWriter, target);
        }

        // now check for anything new
        Long newestTweetId = target.getNewestPostIdLong();
        fetchRange(query, null, newestTweetId, warcWriter, target);
    }
}
