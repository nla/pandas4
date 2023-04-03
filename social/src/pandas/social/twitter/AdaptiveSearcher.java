package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import org.netpreserve.jwarc.HttpRequest;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.WarcCompression;
import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.Json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;
import static org.netpreserve.jwarc.MessageVersion.HTTP_1_0;

public class AdaptiveSearcher {
    private static final Logger log = LoggerFactory.getLogger(AdaptiveSearcher.class);
    private static final URI SW_JS_URL = URI.create("https://twitter.com/sw.js");
    private static final URI SESSION_URL = URI.create("https://twitter.com/search");
    private static final String ADAPTIVE_SEARCH_URL = "https://api.twitter.com/2/search/adaptive.json?include_profile_interstitial_type=1&include_blocking=1&include_blocked_by=1&include_followed_by=1&include_want_retweets=1&include_mute_edge=1&include_can_dm=1&include_can_media_tag=1&include_ext_has_nft_avatar=1&include_ext_is_blue_verified=1&include_ext_verified_type=1&skip_status=1&cards_platform=Web-12&include_cards=1&include_ext_alt_text=true&include_ext_limited_action_results=true&include_quote_count=true&include_reply_count=1&tweet_mode=extended&include_ext_collab_control=true&include_ext_views=true&include_entities=true&include_user_entities=true&include_ext_media_color=true&include_ext_media_availability=true&include_ext_sensitive_media_warning=true&include_ext_trusted_friends_metadata=true&send_error_codes=true&simple_quoted_tweet=true&tweet_search_mode=live&count=20&query_source=spelling_expansion_revert_click&pc=1&spelling_corrections=1&ext=mediaStats%2ChighlightedLabel%2ChasNftAvatar%2CvoiceInfo%2Cenrichments%2CsuperFollowMetadata%2CunmentionInfo,editControl,collab_control,vibe&include_ext_edit_control=true";
    private static final Pattern SET_COOKIE_REGEX = Pattern.compile("document.cookie=\"([^;]*?);");
    private static final Pattern SERVICE_WORKER_JS_REGEX = Pattern.compile("\"(https://abs.twimg.com/responsive-web/client-serviceworker/serviceworker.[a-z0-9]+.js)\"");
    private static final Pattern BEARER_TOKEN_REGEX = Pattern.compile("\"(AAAA[a-zA-Z0-9%]{30,}A)\"");
    public static final int DEFAULT_DELAY_MILLIS = 5000;

    private final String userAgent;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private String bearerToken;
    private Session session;
    private int delayMillis = DEFAULT_DELAY_MILLIS;

    public AdaptiveSearcher(String userAgent) {
        this.userAgent = userAgent;
    }

    public static class QueryState {
        public long tweetCount;
        public Long newestTweetId;
        public Instant newestTweetTimestamp;
        public Long oldestTweetId;
        public Instant oldestTweetTimestamp;
        public Instant createdAt;
        public Instant lastSuccessfulAt;
        public Instant lastAttemptedAt;
        public Long currentRangePosition;
        public Long currentRangeEnd;


        // Legacy fields. To be removed.
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String cursor;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Boolean backfilled;

        public void migrateLegacyFields() {
            if (cursor != null) cursor = null;
            if (backfilled != null) {
                if (!backfilled) {
                    currentRangePosition = oldestTweetId;
                    currentRangeEnd = null;
                }
                backfilled = null;
            }
        }
    }

    private record Session(String cookies, String guestToken, long createdAtMillis) {
        public boolean isTooOld() {
            return System.currentTimeMillis() - createdAtMillis > 1000 * 60 * 10;
        }
    }

    public synchronized void invalidateSession() {
        this.session = null;
    }

    private String findInString(Pattern regex, String string) {
        Matcher matcher = regex.matcher(string);
        if (matcher.find()) return matcher.group(1);
        String errorFile = System.getenv("DEBUG_ERROR_FILE");
        if (errorFile != null) {
            try {
                Files.writeString(Paths.get(errorFile), string);
            } catch (IOException e) {
                log.error("Error writing {}", errorFile, e);
            }
        }
        return null;
    }

    private java.net.http.HttpResponse<String> findInUrl(Pattern pattern, URI uri)
            throws IOException, InterruptedException {
        for (int tries = 0; tries < 3; tries++) {
            log.trace("Finding /{}/ in {}", pattern, uri);
            var request = java.net.http.HttpRequest.newBuilder(uri).GET()
                    .setHeader("User-Agent", userAgent)
                    .build();
            var response = httpClient.send(request,
                    responseInfo -> responseInfo.statusCode() == 200
                            ? BodySubscribers.mapping(BodySubscribers.ofString(UTF_8), body -> findInString(pattern, body))
                            : BodySubscribers.replacing(null));
            if (response.statusCode() != 200) {
                throw new IOException("HTTP status " + response.statusCode() + " from " + uri);
            }
            if (response.body() == null) {
                log.warn("No match for /" + pattern + "/ in " + uri + " (try " + tries + ")");
                Thread.sleep(30000);
                continue;
            }
            return response;
        }
        throw new IOException("No match for /" + pattern + "/ in " + uri);
    }

    private synchronized String getOrFetchBearerToken() throws IOException, InterruptedException {
        if (bearerToken == null) {
            URI serviceWorkerJsUrl = URI.create(findInUrl(SERVICE_WORKER_JS_REGEX, SW_JS_URL).body());
            bearerToken = findInUrl(BEARER_TOKEN_REGEX, serviceWorkerJsUrl).body();
        }
        return bearerToken;
    }

    private synchronized Session getOrCreateSession() throws IOException, InterruptedException {
        if (session == null || session.isTooOld()) {
            log.trace("Creating new session");
            var response = findInUrl(SET_COOKIE_REGEX, SESSION_URL);
            var cookies = new ArrayList<String>();
            cookies.add(response.body());
            response.headers().allValues("set-cookie").forEach(cookie -> cookies.add(cookie.split(";")[0]));
            var guestToken = cookies.stream().filter(cookie -> cookie.startsWith("gt=")).findFirst()
                    .orElseThrow(() -> new IOException("Missing gt cookie"))
                    .substring(3);
            session = new Session(String.join("; ", cookies), guestToken, System.currentTimeMillis());
        }
        return session;
    }

    private void innerSearch(String query, WarcWriter warcWriter, QueryState state) throws IOException, InterruptedException {
        int page = 0;
        String cursor = null;
        do {
            log.trace("Searching for '{}' with cursor '{}'", query, cursor);
            String url = ADAPTIVE_SEARCH_URL + "&q=" + URLEncoder.encode(query, UTF_8);
            if (cursor != null) {
                url += "&cursor=" + URLEncoder.encode(cursor, UTF_8);
            }

            long startNanos = System.nanoTime();
            state.lastAttemptedAt = Instant.now();
            URI uri = URI.create(url);
            Session session = getOrCreateSession();
            var httpRequest = new HttpRequest.Builder("GET", uri.getRawPath() + "?" + uri.getRawQuery())
                    .version(HTTP_1_0)
                    .addHeader("Authorization", "Bearer " + getOrFetchBearerToken())
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
                break;
            }
            if (httpResponse.headers().first("x-rate-limit-remaining").map(Integer::parseInt).orElse(10) < 10) {
                invalidateSession();
            }
            var adaptiveResponse = Json.mapper.readValue(httpResponse.body().stream(), AdaptiveSearch.class);
            var tweets = adaptiveResponse.tweets();
            if (log.isTraceEnabled()) {
                log.trace("Got tweets {}", tweets.stream().map(Tweet::id).toList());
            }
            if (!tweets.isEmpty()) {
                state.lastSuccessfulAt = state.lastAttemptedAt;
                state.tweetCount += tweets.size();
                if (page == 0) {
                    var newestTweet = tweets.get(0);
                    if (state.newestTweetId == null || newestTweet.id() > state.newestTweetId) {
                        state.newestTweetId = newestTweet.id();
                        state.newestTweetTimestamp = newestTweet.createdAt();
                    }
                }
                var oldestTweet = tweets.get(tweets.size() - 1);
                if (state.oldestTweetId == null || oldestTweet.id() < state.oldestTweetId) {
                    state.oldestTweetId = oldestTweet.id();
                    state.oldestTweetTimestamp = oldestTweet.createdAt();
                }
                state.currentRangePosition = oldestTweet.id();
            }
            cursor = adaptiveResponse.nextCursor();
            page++;
            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            long millisToSleep = delayMillis - elapsedMillis;
            if (millisToSleep > 0) {
                log.trace("Sleeping for {}ms", millisToSleep);
                Thread.sleep(millisToSleep);
            }
        } while (cursor != null);
    }

    public void fetchRange(String query, Long start, Long end, WarcWriter warcWriter, QueryState state) throws IOException, InterruptedException {
        state.currentRangeEnd = end;

        if (start != null) query += " max_id:" + start;
        if (end != null) query += " since_id:" + end;
        innerSearch(query, warcWriter, state);

        // we've finished the current range, so clear it
        state.currentRangePosition = null;
        state.currentRangeEnd = null;
    }

    public void search(String query, WarcWriter warcWriter, QueryState state) throws IOException, InterruptedException {
        state.migrateLegacyFields();

        // if we were interrupted, first finish the current range
        if (state.currentRangePosition != null) {
            log.info("Resuming interrupted search for '{}' from {} to {}", query,
                    state.currentRangePosition, state.currentRangeEnd);
            fetchRange(query, state.currentRangePosition + 1, state.currentRangeEnd, warcWriter, state);
        }

        // now check for anything new
        fetchRange(query, null, state.newestTweetId, warcWriter, state);
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        WarcCompression compression = WarcCompression.NONE;
        List<String> queries = new ArrayList<>();
        WritableByteChannel outputChannel = Channels.newChannel(System.out);
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0";
        Path stateFile = null;
        Integer delayMillis = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-A", "--user-agent" -> userAgent = args[++i];
                case "-c" -> compression = WarcCompression.valueOf(args[++i]);
                case "-d", "--delay-millis" -> delayMillis = Integer.parseInt(args[++i]);
                case "-s", "--state-file" -> stateFile = Paths.get(args[++i]);
                case "-h", "--help" -> {
                    System.out.println("Usage: tootkeeper twitter [options] query");
                    System.out.println("  -A, --user-agent user-agent  User agent (default: " + userAgent + ")");
                    System.out.println("  -c compression               WARC compression " + Arrays.asList(WarcCompression.values()));
                    System.out.println("  -d, --delay-millis millis    Delay between requests (default: " + DEFAULT_DELAY_MILLIS + ")");
                    System.out.println("  -s, --state-file file        State file for incremental archiving");
                    System.out.println("  -o file                      Output file (default: stdout)");
                    System.exit(0);
                }
                case "-o" -> outputChannel = FileChannel.open(Paths.get(args[++i]), CREATE, WRITE, TRUNCATE_EXISTING);
                default -> {
                    if (args[i].startsWith("-")) {
                        System.err.println("Unknown option " + args[i]);
                        System.exit(1);
                    }
                    queries.add(args[i]);
                }
            }
        }

        if (queries.isEmpty()) {
            System.err.println("Missing query");
            System.exit(1);
        }

        SortedMap<String, QueryState> stateMap = new TreeMap<>();
        if (stateFile != null && Files.exists(stateFile)) {
            stateMap = Json.mapper.readValue(stateFile.toFile(), new TypeReference<>() {
            });
        }

        if (stateFile != null) writeStateOnShutdown(stateFile, stateMap);

        var searcher = new AdaptiveSearcher(userAgent);
        if (delayMillis != null) searcher.setDelayMillis(delayMillis);

        var finalStateMap = stateMap;
        queries.sort(Comparator.comparing(query -> {
            var state = finalStateMap.get(query);
            if (state == null || state.lastAttemptedAt == null) return Instant.MIN;
            return state.lastAttemptedAt;
        }));

        try (WarcWriter warcWriter = new WarcWriter(outputChannel, compression)) {
            for (var query : queries) {
                searcher.search(query, warcWriter, stateMap.computeIfAbsent(query, q -> new QueryState()));
                writeState(stateFile, stateMap);
            }
        }
    }

    private static void writeStateOnShutdown(Path finalStateFile, SortedMap<String, QueryState> finalStateMap) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> writeState(finalStateFile, finalStateMap)));
    }

    private synchronized static void writeState(Path stateFile, SortedMap<String, QueryState> stateMap) {
        try {
            log.trace("Updating state file {}", stateFile);
            Json.mapper.writeValue(stateFile.toFile(), stateMap);
        } catch (IOException e) {
            log.error("Failed to write state file {}", stateFile, e);
        }
    }
}
