package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.SocialJson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SessionManager {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static final URI SW_JS_URL = URI.create("https://twitter.com/sw.js");
    private static final Pattern SERVICE_WORKER_JS_REGEX = Pattern.compile("\"(https://abs.twimg.com/responsive-web/client-serviceworker/serviceworker.[a-z0-9]+.js)\"");
    private static final Pattern BEARER_TOKEN_REGEX = Pattern.compile("\"(AAAA[a-zA-Z0-9%]{30,}A)\"");

    private Session session;
    private String bearerToken;
    private final String userAgent;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public SessionManager(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public record Session(String cookies, String guestToken, long createdAtMillis) {
        public boolean isTooOld() {
            return System.currentTimeMillis() - createdAtMillis > 1000 * 60 * 10;
        }
    }

    public synchronized void invalidateSession() {
        this.session = null;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TokenResponse(String guestToken) {
    }

    public synchronized Session getOrCreateSession() throws IOException, InterruptedException {
        if (session == null || session.isTooOld()) {
            log.trace("Creating new session");
//            var response = findInUrl(SET_COOKIE_REGEX, SESSION_URL);
            var request = HttpRequest.newBuilder(URI.create("https://api.twitter.com/1.1/guest/activate.json"))
                    .setHeader("User-Agent", userAgent)
                    .setHeader("Authorization", "Bearer " + getOrFetchBearerToken())
                    .POST(HttpRequest.BodyPublishers.noBody());
            var response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
            var cookies = new ArrayList<String>();
            response.headers().allValues("set-cookie").forEach(cookie -> cookies.add(cookie.split(";")[0]));
            var guestToken = SocialJson.mapper.readValue(response.body(), TokenResponse.class).guestToken();
            session = new Session(String.join("; ", cookies), guestToken, System.currentTimeMillis());
        }
        return session;
    }

    public synchronized String getOrFetchBearerToken() throws IOException, InterruptedException {
        if (bearerToken == null) {
            URI serviceWorkerJsUrl = URI.create(findInUrl(SERVICE_WORKER_JS_REGEX, SW_JS_URL).body());
            bearerToken = findInUrl(BEARER_TOKEN_REGEX, serviceWorkerJsUrl).body();
        }
        return bearerToken;
    }

    private HttpResponse<String> findInUrl(Pattern pattern, URI uri)
            throws IOException, InterruptedException {
        for (int tries = 0; tries < 3; tries++) {
            log.trace("Finding /{}/ in {}", pattern, uri);
            var request = HttpRequest.newBuilder(uri).GET()
                    .setHeader("User-Agent", userAgent)
                    .build();
            var response = httpClient.send(request,
                    responseInfo -> responseInfo.statusCode() == 200
                            ? HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(UTF_8), body -> findInString(pattern, body))
                            : HttpResponse.BodySubscribers.replacing(null));
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
}
