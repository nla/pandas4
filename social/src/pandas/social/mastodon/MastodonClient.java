package pandas.social.mastodon;

import dev.failsafe.*;
import org.jetbrains.annotations.NotNull;
import org.netpreserve.jwarc.HttpRequest;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.LengthedBody;
import org.netpreserve.jwarc.WarcWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.SocialJson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
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
            RetryPolicy.<HttpResponse>builder()
                    .handleResultIf(response -> response.status() >= 400)
                    .handle(IOException.class)
                    .withBackoff(10, 10 * 60, ChronoUnit.SECONDS, 4)
                    .onRetry(e -> log.warn("Failure #{}. Retrying.", e.getAttemptCount(), e.getLastException()))
                    .build(),
            RateLimiter.<HttpResponse>smoothBuilder(100, Duration.ofMinutes(5))
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
        var params = new Params();
        params.add("exclude_replies", "false");
        params.add("since_id", sinceId);
        params.add("max_id", maxId);
        return Arrays.asList(sendRequest("/api/v1/accounts/" + accountId + "/statuses?" + params, Status[].class));
    }

    public List<Status> getPublicTimeline(Boolean local, Integer limit, String minId) throws IOException {
        var params = new Params();
        params.add("local", local);
        params.add("limit", limit);
        params.add("max_id", minId);
        return Arrays.asList(sendRequest("/api/v1/timelines/public?" + params, Status[].class));
    }

    private static class Params {
        private StringBuilder queryString = new StringBuilder();

        public void add(String name, String value) {
            if (value == null) return;
            if (!queryString.isEmpty()) queryString.append('&');
            queryString.append(name).append('=').append(URLEncoder.encode(value, UTF_8));
        }

        public void add(String name, Boolean value) {
            if (value == null) return;
            add(name, value.toString());
        }

        public void add(String name, Number value) {
            if (value == null) return;
            add(name, value.toString());
        }

        public String toString() {
            return queryString.toString();
        }
    }

    @NotNull
    private <T> T sendRequest(String path, Class<T> returnType) throws IOException {
        HttpResponse httpResponse = failsafe.get(() -> sendRequestInner(path));
        if (httpResponse.status() != 200) {
            throw new IOException("Unexpected response " + httpResponse.status() + " " + httpResponse.reason() + " from " + server + path);
        }
        return SocialJson.mapper.readValue(httpResponse.body().stream(), returnType);
    }

    @NotNull
    private HttpResponse sendRequestInner(String path) throws IOException {
        var uri = URI.create(server + path);
        log.trace("GET {}", uri);
        var builder = new HttpRequest.Builder("GET", uri.getRawPath() + "?" + uri.getRawQuery())
                .version(HTTP_1_0);
        if (userAgent != null) {
            builder.addHeader("User-Agent", userAgent);
        }
        var httpRequest = builder
                .addHeader("Connection", "close")
                .addHeader("Host", uri.getHost())
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        warcWriter.fetch(uri, httpRequest, buffer);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
        byteBuffer.flip();
        var body = LengthedBody.create(Channels.newChannel(new ByteArrayInputStream(buffer.toByteArray())), byteBuffer, buffer.size());
        var httpResponse = HttpResponse.parse(body);
        log.trace("{} {} ({} bytes) from {}", httpResponse.status(), httpResponse.reason(),
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
