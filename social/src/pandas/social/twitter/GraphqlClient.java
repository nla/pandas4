package pandas.social.twitter;

import dev.failsafe.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netpreserve.jwarc.HttpRequest;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.SocialJson;

import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.netpreserve.jwarc.MessageVersion.HTTP_1_0;

public class GraphqlClient {
    private static final Logger log = LoggerFactory.getLogger(GraphqlClient.class);
    private final SessionManager sessionManager;
    private final WarcWriter warcWriter;
    private final static FailsafeExecutor<HttpResponse> failsafe = Failsafe.with(
            RetryPolicy.<HttpResponse>builder()
                    .handleResultIf(response -> response.status() >= 400)
                    .handle(IOException.class)
                    .withBackoff(10, 10 * 60, ChronoUnit.SECONDS, 4)
                    .onRetry(e -> log.warn("Failure #{}. Retrying.", e.getAttemptCount(), e.getLastException()))
                    .build(),
            RateLimiter.<HttpResponse>smoothBuilder(100, Duration.ofMinutes(15))
                    .withMaxWaitTime(Duration.ofMinutes(5))
                    .build(),
            CircuitBreaker.<HttpResponse>builder()
                    .handleResultIf(response -> response.status() >= 400)
                    .handle(IOException.class)
                    .onOpen(e -> log.warn("Circuit breaker open"))
                    .onClose(e -> log.warn("Circuit breaker closed"))
                    .onHalfOpen(e -> log.warn("Circuit breaker half-opened"))
                    .withFailureThreshold(8)
                    .withSuccessThreshold(1)
                    .withDelay(Duration.ofMinutes(30))
                    .build());

    public GraphqlClient(SessionManager sessionManager, WarcWriter warcWriter) {
        this.sessionManager = sessionManager;
        this.warcWriter = warcWriter;
    }

    public UserV2 userByScreenName(@NotNull String screenName) throws IOException, InterruptedException {
        Map<String, Object> variables = Map.of("screen_name", screenName, "withSafetyModeUserFields", true);
        var features = "{\"blue_business_profile_image_shape_enabled\":true,\"responsive_web_graphql_exclude_directive_enabled\":true,\"verified_phone_label_enabled\":false,\"responsive_web_graphql_skip_user_profile_image_extensions_enabled\":false,\"responsive_web_graphql_timeline_navigation_enabled\":true}";
        var data = sendRequest("sLVLhk0bGj3MVFEKTdax1w/UserByScreenName", variables, features, UserResponse.class).data();
        if (data.user() != null && data.user().result() instanceof UserV2 user) {
            return user;
        }
        return null;
    }

    public TimelineV2 tweetsAndReplies(@NotNull String userId, @Nullable String cursor) throws IOException, InterruptedException {
        Map<String, Object> variables = new HashMap<>(Map.of(
                "userId", userId,
                "count", 40,
                "includePromotedContent", true,
                "withCommunity", true,
                "withVoice", true,
                "withV2Timeline", true));
        if (cursor != null) variables.put("cursor", cursor);
        var features = "{\"blue_business_profile_image_shape_enabled\":true,\"responsive_web_graphql_exclude_directive_enabled\":true,\"verified_phone_label_enabled\":false,\"responsive_web_graphql_timeline_navigation_enabled\":true,\"responsive_web_graphql_skip_user_profile_image_extensions_enabled\":false,\"tweetypie_unmention_optimization_enabled\":true,\"vibe_api_enabled\":true,\"responsive_web_edit_tweet_api_enabled\":true,\"graphql_is_translatable_rweb_tweet_is_translatable_enabled\":true,\"view_counts_everywhere_api_enabled\":true,\"longform_notetweets_consumption_enabled\":true,\"tweet_awards_web_tipping_enabled\":false,\"freedom_of_speech_not_reach_fetch_enabled\":false,\"standardized_nudges_misinfo\":true,\"tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled\":false,\"interactive_text_enabled\":true,\"responsive_web_text_conversations_enabled\":false,\"longform_notetweets_rich_text_read_enabled\":true,\"responsive_web_enhance_cards_enabled\":false}";
        var data = sendRequest("zQxfEr5IFxQ2QZ-XMJlKew/UserTweetsAndReplies", variables, features, TimelineV2.Response.class).data();
        if (data.user() == null) return null;
        return data.user().result().timeline_v2();
    }

    public TimelineV2 searchTimeline(@NotNull String query, @Nullable String cursor) throws IOException, InterruptedException {
        if (query.isBlank()) throw new IllegalArgumentException("Blank query");
        Map<String, Object> variables = new HashMap<>(Map.of(
                "rawQuery", query,
                "count", 20,
                "product", "Latest"));
        if (cursor != null) variables.put("cursor", cursor);
        var features = "{\"blue_business_profile_image_shape_enabled\":true,\"responsive_web_graphql_exclude_directive_enabled\":true,\"verified_phone_label_enabled\":false,\"responsive_web_graphql_timeline_navigation_enabled\":true,\"responsive_web_graphql_skip_user_profile_image_extensions_enabled\":false,\"tweetypie_unmention_optimization_enabled\":true,\"vibe_api_enabled\":true,\"responsive_web_edit_tweet_api_enabled\":true,\"graphql_is_translatable_rweb_tweet_is_translatable_enabled\":true,\"view_counts_everywhere_api_enabled\":true,\"longform_notetweets_consumption_enabled\":true,\"tweet_awards_web_tipping_enabled\":false,\"freedom_of_speech_not_reach_fetch_enabled\":false,\"standardized_nudges_misinfo\":true,\"tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled\":false,\"interactive_text_enabled\":true,\"responsive_web_text_conversations_enabled\":false,\"longform_notetweets_rich_text_read_enabled\":true,\"responsive_web_enhance_cards_enabled\":false}";
        var data = sendRequest("WeHGEHYtJA0sfOOFIBMt8g/SearchTimeline", variables, features, TimelineV2.SearchResponse.class).data();
        if (data.searchByRawQuery() == null) return null;
        return data.searchByRawQuery().searchTimeline();
    }

    public <T> T sendRequest(@NotNull String endpoint,
                            @NotNull Map<String, Object> variables,
                            @NotNull String features,
                            @NotNull Class<T> responseClass) throws IOException, InterruptedException {
        String url = "https://twitter.com/i/api/graphql/" + endpoint +
                "?variables=" + URLEncoder.encode(SocialJson.mapper.writeValueAsString(variables), StandardCharsets.UTF_8) +
                "&features=" + URLEncoder.encode(features, StandardCharsets.UTF_8);
        var uri = URI.create(url);
        var session = sessionManager.getOrCreateSession();
        var httpRequest = new HttpRequest.Builder("GET", uri.getRawPath() + "?" + uri.getRawQuery())
                .version(HTTP_1_0)
                .addHeader("Authorization", "Bearer " + sessionManager.getOrFetchBearerToken())
                .addHeader("Connection", "close")
                .addHeader("Cookie", session.cookies())
                .addHeader("Host", uri.getHost())
                .addHeader("User-Agent", sessionManager.getUserAgent())
                .addHeader("x-guest-token", session.guestToken())
                .addHeader("x-twitter-active-user", "yes")
                .addHeader("x-twitter-client-language", "en")
                .build();
        HttpResponse httpResponse = failsafe.get(() -> sendRequestInner(uri, httpRequest));
        if (httpResponse.status() != 200) {
            log.error("Status {} from {}", httpResponse.status(), uri);
            throw new IOException("Status " + httpResponse.status() + " from " + uri);
        }
        if (httpResponse.headers().first("x-rate-limit-remaining").map(Integer::parseInt).orElse(10) < 10) {
            sessionManager.invalidateSession();
        }
        return SocialJson.mapper.readValue(httpResponse.body().stream(), responseClass);
    }

    @NotNull
    private HttpResponse sendRequestInner(URI uri, HttpRequest httpRequest) throws IOException {
        log.trace("GET {}", uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        warcWriter.fetch(uri, httpRequest, buffer);
        var httpResponse = HttpResponse.parse(Channels.newChannel(new ByteArrayInputStream(buffer.toByteArray())));
        log.trace("{} {} ({} bytes) from {}", httpResponse.status(), httpResponse.reason(),
                httpResponse.body().size(), uri);
        return httpResponse;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var sessionManager = new SessionManager(System.getenv().getOrDefault("USER_AGENT", "test"));
        var client = new GraphqlClient(sessionManager, new WarcWriter(System.out));
        System.out.println(client.userByScreenName(args[0]));
    }
}
