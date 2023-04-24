package pandas.social.mastodon;

import org.jetbrains.annotations.NotNull;
import org.netpreserve.jwarc.HttpRequest;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.WarcWriter;
import pandas.social.SocialJson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.netpreserve.jwarc.MessageVersion.HTTP_1_0;

public class MastodonClient {
    private final String server;
    private final String userAgent;
    private final WarcWriter warcWriter;

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

    public List<Status> getAccountStatuses(String accountId) throws IOException {
        return Arrays.asList(sendRequest("/api/v1/accounts/" + accountId + "/statuses?exclude_replies=false", Status[].class));
    }

    @NotNull
    private <T> T sendRequest(String path, Class<T> returnType) throws IOException {
        var uri = URI.create(server + path);
        var httpRequest = new HttpRequest.Builder("GET", uri.getRawPath() + "?" + uri.getRawQuery())
                .version(HTTP_1_0)
                .addHeader("Connection", "close")
                .addHeader("Host", uri.getHost())
                .addHeader("User-Agent", userAgent)
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        warcWriter.fetch(uri, httpRequest, buffer);
        var httpResponse = HttpResponse.parse(Channels.newChannel(new ByteArrayInputStream(buffer.toByteArray())));
        if (httpResponse.status() != 200) {
            throw new IOException("Status " + httpResponse.status() + " from " + uri);
        }
        return SocialJson.mapper.readValue(httpResponse.body().stream(), returnType);
    }

    public static void main(String[] args) throws IOException {
        String server = args[0];
        String acct = args[1];
        try (var warcWriter = new WarcWriter(System.out)) {
            MastodonClient client = new MastodonClient(server, "test", warcWriter);
            Account account = client.lookupAccount(acct);
            System.out.println(account);
            System.out.println(client.getAccountStatuses(account.id()));
        }
    }
}
