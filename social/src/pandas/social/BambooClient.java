package pandas.social;

import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class BambooClient {
    private static final Logger log = LoggerFactory.getLogger(BambooClient.class);

    public static final AnonymousAuthenticationToken ANONYMOUS = new AnonymousAuthenticationToken
            ("key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    private final long collectionId;
    private final long crawlSeriesId;
    private final String baseUrl;
    private final OAuth2AuthorizedClientManager oauth2ClientManager;


    public BambooClient(@Autowired(required = false) OAuth2AuthorizedClientManager oauth2ClientManager,
                        SocialBambooConfig config) {
        this.oauth2ClientManager = oauth2ClientManager;
        collectionId = config.getCollectionId();
        baseUrl = config.getUrl().replaceFirst("/+$", "");
        crawlSeriesId = config.getCrawlSeriesId();
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
        return URI.create(baseUrl + "/warcs/" + warcId).toURL().openStream();
    }

    public long createCrawl(String name) throws IOException {
        byte[] body = ("name=" + UriUtils.encode(name, UTF_8) +
                "&crawlSeriesId=" + crawlSeriesId).getBytes();
        var connection = (HttpURLConnection)URI.create(baseUrl + "/crawls/new").toURL().openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        connection.setRequestProperty("Content-Length", String.valueOf(body.length));
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
        log.info("Importing WARC {} to crawl {}", filename, crawlId);
        var connection = (HttpURLConnection)URI.create(baseUrl + "/crawls/" + crawlId + "/warcs/" + filename)
                .toURL().openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/warc");
        connection.setRequestProperty("Content-Length", String.valueOf(length));
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

    public record WarcRef (long id, Long urlCount) {
    }
}
