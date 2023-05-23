package pandas.social;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class BambooClient {
    private static final Logger log = LoggerFactory.getLogger(BambooClient.class);

    public static final AnonymousAuthenticationToken ANONYMOUS = new AnonymousAuthenticationToken
            ("key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    private final static FailsafeExecutor<Integer> failsafe = Failsafe.with(
            RetryPolicy.<Integer>builder()
                    .abortOn(FileNotFoundException.class)
                    .handle(IOException.class)
                    .withBackoff(1, 10 * 60, ChronoUnit.SECONDS, 4)
                    .onRetry(e -> log.warn("Failure #{}. Retrying.", e.getAttemptCount(), e.getLastException()))
                    .build());
    private final long collectionId;
    private final String baseUrl;
    private final OAuth2AuthorizedClientManager oauth2ClientManager;


    public BambooClient(@Autowired(required = false) OAuth2AuthorizedClientManager oauth2ClientManager,
                        SocialBambooConfig config) {
        this.oauth2ClientManager = oauth2ClientManager;
        collectionId = config.getCollectionId();
        baseUrl = config.getUrl().replaceFirst("/+$", "");
    }

    public List<Long> listWarcIds() throws IOException {
        var connection = (HttpURLConnection)URI.create(baseUrl + "/collections/" + collectionId + "/warcs/json").toURL().openConnection();
        authorize(connection);
        try (InputStream inputStream = connection.getInputStream()) {
            var refs = SocialJson.mapper.readValue(inputStream, WarcRef[].class);
            return Arrays.stream(refs).map(ref -> ref.id).toList();
        }
    }

    public InputStream openWarc(long warcId) throws IOException {
        return URI.create(urlForWarc(warcId)).toURL().openStream();
    }

    /**
     * Opens a WARC file in a way that avoids holding a connection open with long pauses when reading from the file.
     * This is achieved by using a {@link BufferedInputStream} with a large buffer and a {@link UrlRangeInputStream}
     * that requests a range of bytes from the server.
     */
    public InputStream openWarcWithRangeBuffering(long warcId) throws IOException {
        int bufferSize = 16 * 1024 * 1024;
        return new BufferedInputStream(new UrlRangeInputStream(URI.create(urlForWarc(warcId)).toURL()), bufferSize);
    }

    public static class UrlRangeInputStream extends InputStream {
        private long position = 0;
        private boolean eof = false;
        private final URL url;

        public UrlRangeInputStream(URL url) {
            this.url = url;
        }

        @Override
        public int read() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int read(byte @NotNull [] buffer, int off, int len) throws IOException {
            if (len == 0) return 0;
            if (eof) return -1;
            return failsafe.get(() -> {
                log.debug("GET range bytes={}-{} from {}", position, position + len - 1, url);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Range", "bytes=" + position + "-" + (position + len - 1));
                try (InputStream stream = connection.getInputStream()) {
                    if (connection.getResponseCode() != 206) {
                        throw new IOException("Expected response code 206 but got " + connection.getResponseCode() + " from " + url);
                    }
                    int n = IOUtils.read(stream, buffer, off, len);
                    long contentLength = connection.getContentLengthLong();
                    if (contentLength != -1 && n < contentLength) {
                        throw new EOFException("Content-Length suggested " + contentLength + " bytes but only got " + n);
                    }
                    if (n < len) eof = true;
                    position += n;
                    return n;
                }
            });
        }
    }

    public String urlForWarc(long warcId) {
        return baseUrl + "/warcs/" + warcId;
    }

    public long createCrawl(long crawlSeriesId, String name) throws IOException {
        byte[] body = ("name=" + UriUtils.encode(name, UTF_8) +
                "&crawlSeriesId=" + crawlSeriesId).getBytes();
        var connection = (HttpURLConnection)URI.create(baseUrl + "/crawls/new").toURL().openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        connection.setFixedLengthStreamingMode(body.length);
        authorize(connection);
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(body);
        }
        try (InputStream stream = connection.getInputStream()) {
            IOUtils.consume(stream);
        }
        var location = connection.getHeaderField("Location");
        if (location == null) throw new IOException("Expected Location header in response from " + connection.getURL());
        log.info("Created crawl {}", location);
        return Long.parseLong(URI.create(location).getPath().replaceFirst(".*/", ""));
    }

    public long putWarcIfNotExists(long crawlId, String filename, ReadableByteChannel channel, long length) throws IOException {
        if (crawlId < 0) throw new IllegalArgumentException("crawlId can't be negative: " + crawlId);
        log.info("Importing WARC {} to crawl {}", filename, crawlId);
        var connection = (HttpURLConnection)URI.create(baseUrl + "/crawls/" + crawlId + "/warcs/" + filename)
                .toURL().openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/warc");
        connection.setFixedLengthStreamingMode(length);
        authorize(connection);
        try (OutputStream stream = connection.getOutputStream()) {
            IOUtils.copy(Channels.newInputStream(channel), stream);
        }
        try (InputStream stream = connection.getInputStream()) {
            IOUtils.consume(stream);
        }
        var location = connection.getHeaderField("Location");
        if (location == null) throw new IOException("Expected Location header in response from " + connection.getURL());
        log.info("Uploaded warc {}", location);
        return Long.parseLong(URI.create(location).getPath().replaceFirst(".*/", ""));
    }

    public void authorize(HttpURLConnection connection) {
        if (oauth2ClientManager == null) return;
        var oauthRequest = OAuth2AuthorizeRequest.withClientRegistrationId("oidc")
                .principal(ANONYMOUS)
                .build();
        var authorizedClient = oauth2ClientManager.authorize(oauthRequest);
        String token = authorizedClient.getAccessToken().getTokenValue();
        connection.setRequestProperty("Authorization", "Bearer " + token);
    }

    public List<WarcRef> syncWarcsInCollection(@Nullable String resumptionToken, int limit) throws IOException {
        var connection = (HttpURLConnection)URI.create(baseUrl + "/collections/" + collectionId +
                        "/warcs/sync?limit=" + limit +
                        (resumptionToken == null ? "" : "&after=" + resumptionToken))
                .toURL().openConnection();
        authorize(connection);
        try (InputStream inputStream = connection.getInputStream()) {
            return Arrays.asList(SocialJson.mapper.readValue(inputStream, WarcRef[].class));
        }
    }

    public record WarcRef (long id, String resumptionToken, Long urlCount) {
    }
}
