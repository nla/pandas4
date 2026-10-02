package pandas.search;

import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.MessageBody;
import pandas.gather.BotBlocker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Detects bot-blocking responses - based on Heritrix's BotBlockDetector. */
final class BotBlockDetector {
    private static final int BODY_PREFIX_LENGTH = 8000;

    private BotBlockDetector() {
    }

    static BotBlocker detect(HttpResponse response) {
        BodyPrefix bodyPrefix = new BodyPrefix(response);
        if (detectAkamai(response)) return BotBlocker.AKAMAI;
        if (detectAnubisRedirect(response)) return BotBlocker.ANUBIS;
        if (detectAnubisChallenge(response, bodyPrefix)) return BotBlocker.ANUBIS;
        if (detectCloudflareChallenge(response)) return BotBlocker.CLOUDFLARE;
        if (detectCloudflareBlock(response, bodyPrefix)) return BotBlocker.CLOUDFLARE;
        if (detectDataDome(response)) return BotBlocker.DATADOME;
        if (detectIncapsula(response, bodyPrefix)) return BotBlocker.INCAPSULA;
        return null;
    }

    private static boolean detectAkamai(HttpResponse response) {
        return response.status() == 403 &&
                "AkamaiGHost".equals(header(response, "server"));
    }

    private static boolean detectAnubisRedirect(HttpResponse response) {
        String location = header(response, "location");
        return response.status() == 307 && location != null &&
                location.contains("/.within.website/?redir=");
    }

    private static boolean detectAnubisChallenge(HttpResponse response, BodyPrefix bodyPrefix) {
        return response.status() == 200 &&
                startsWith(header(response, "set-cookie"), "techaro.lol-anubis-") &&
                bodyPrefix.contains("<script id=\"anubis_challenge\"");
    }

    private static boolean detectCloudflareChallenge(HttpResponse response) {
        return "challenge".equalsIgnoreCase(header(response, "cf-mitigated"));
    }

    private static boolean detectCloudflareBlock(HttpResponse response, BodyPrefix bodyPrefix) {
        return response.status() == 403 &&
                "cloudflare".equalsIgnoreCase(header(response, "server")) &&
                bodyPrefix.contains("<h1 data-translate=\"block_headline\">Sorry, you have been blocked</h1>");
    }

    private static boolean detectDataDome(HttpResponse response) {
        return header(response, "x-dd-b") != null &&
                "protected".equalsIgnoreCase(header(response, "x-datadome"));
    }

    private static boolean detectIncapsula(HttpResponse response, BodyPrefix bodyPrefix) {
        if (header(response, "x-iinfo") == null) return false;

        if (response.status() == 404) {
            return bodyPrefix.contains("Incapsula incident ID:");
        }

        if (response.status() != 200) return false;
        String body = bodyPrefix.get();
        return body != null && containsIncapsulaChallengeWrapper(body);
    }

    private static String header(HttpResponse response, String name) {
        return response.headers().first(name).orElse(null);
    }

    private static boolean startsWith(String value, String prefix) {
        return value != null && value.startsWith(prefix);
    }

    private static boolean containsIncapsulaChallengeWrapper(String html) {
        return html.contains("main-iframe") &&
                html.contains("/_Incapsula_Resource?") &&
                html.contains("incident_id=") &&
                html.contains("Incapsula incident ID:");
    }

    private static final class BodyPrefix {
        private final HttpResponse response;
        private boolean loaded;
        private String value;

        private BodyPrefix(HttpResponse response) {
            this.response = response;
        }

        private boolean contains(String signature) {
            String body = get();
            return body != null && body.contains(signature);
        }

        private String get() {
            if (!loaded) {
                // The response body is a stream, so load the bounded prefix at most once.
                value = htmlBodyPrefix(response);
                loaded = true;
            }
            return value;
        }

        private static String htmlBodyPrefix(HttpResponse response) {
            if (!hasHtmlContentType(response)) return null;
            try {
                MessageBody body = response.bodyDecoded();
                byte[] prefix = body.stream().readNBytes(BODY_PREFIX_LENGTH);
                return new String(prefix, StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static boolean hasHtmlContentType(HttpResponse response) {
        String type = header(response, "content-type");
        return type == null || matchesMediaType(type, "text/html") ||
                matchesMediaType(type, "application/xhtml+xml");
    }

    private static boolean matchesMediaType(String value, String expected) {
        if (!value.regionMatches(true, 0, expected, 0, expected.length())) return false;
        int i = expected.length();
        while (i < value.length() && Character.isWhitespace(value.charAt(i))) i++;
        return i == value.length() || value.charAt(i) == ';';
    }
}
