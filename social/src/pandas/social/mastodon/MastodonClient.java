package pandas.social.mastodon;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RateLimiter;
import dev.failsafe.RetryPolicy;
import org.jetbrains.annotations.NotNull;
import org.netpreserve.jwarc.HttpRequest;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.SocialJson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.netpreserve.jwarc.MessageVersion.HTTP_1_0;

public class MastodonClient {
    private final static Logger log = LoggerFactory.getLogger(MastodonClient.class);
    private final String server;
    private final String userAgent;
    private final WarcWriter warcWriter;
    private static final FailsafeExecutor<HttpResponse> failsafe = Failsafe.with(
            RateLimiter.<HttpResponse>smoothBuilder(100, Duration.ofMinutes(5))
                    .withMaxWaitTime(Duration.ofMinutes(5))
                    .build(),
            RetryPolicy.<HttpResponse>builder()
                    .handleResultIf(response -> response.status() != 200)
                    .handle(IOException.class)
                    .withBackoff(10, 10 * 60, ChronoUnit.SECONDS, 4)
                    .onRetry(e -> log.warn("Failure #{}. Retrying.", e.getAttemptCount(), e.getLastException()))
                    .build());

    public MastodonClient(String server, String userAgent, WarcWriter warcWriter) {
        if (!server.startsWith("https://") || server.startsWith("http://")) {
            server = "https://" + server;
        }
        this.server = server.replaceFirst("/+$", "");
        this.userAgent = userAgent;
        this.warcWriter = warcWriter;
    }

    public Account lookupAccount(String acct) throws IOException {
        return sendRequest("/api/v1/accounts/lookup?acct=" + URLEncoder.encode(acct, UTF_8), Account.class);
    }

    public List<Status> getAccountStatuses(String accountId, String sinceId, String maxId) throws IOException {
        StringBuilder params = new StringBuilder("exclude_replies=false");
        if (sinceId != null) params.append("&since_id=").append(URLEncoder.encode(sinceId, UTF_8));
        if (maxId != null) params.append("&max_id=").append(URLEncoder.encode(maxId, UTF_8));
        return Arrays.asList(sendRequest("/api/v1/accounts/" + accountId + "/statuses?" + params, Status[].class));
    }

    @NotNull
    private <T> T sendRequest(String path, Class<T> returnType) throws IOException {
        HttpResponse httpResponse = failsafe.get(() -> sendRequestInner(path));
        return SocialJson.mapper.readValue(httpResponse.body().stream(), returnType);
    }

    @NotNull
    private HttpResponse sendRequestInner(String path) throws IOException {
        var uri = URI.create(server + path);
        log.debug("GET {}", uri);
        var httpRequest = new HttpRequest.Builder("GET", uri.getRawPath() + "?" + uri.getRawQuery())
                .version(HTTP_1_0)
                .addHeader("Connection", "close")
                .addHeader("Host", uri.getHost())
                .addHeader("User-Agent", userAgent)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        warcWriter.fetch(uri, httpRequest, buffer);
        var httpResponse = HttpResponse.parse(Channels.newChannel(new ByteArrayInputStream(buffer.toByteArray())));
        log.debug("{} {} ({} bytes) from {}", httpResponse.status(), httpResponse.reason(),
                httpResponse.body().size(), uri);
        return httpResponse;
    }

    public static void main(String[] args) throws IOException {
        String server = args[0];
        String acct = args[1];
        try (var warcWriter = new WarcWriter(System.out)) {
            MastodonClient client = new MastodonClient(server, "test", warcWriter);
            Account account = client.lookupAccount(acct);
            System.out.println(account);
            System.out.println(client.getAccountStatuses(account.id(), null, null));
        }
    }
}
