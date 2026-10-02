package pandas.search;

import org.junit.jupiter.api.Test;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.MediaType;
import pandas.gather.BotBlocker;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BotBlockDetectorTest {
    @Test
    void detectsAkamai() {
        assertEquals(BotBlocker.AKAMAI, BotBlockDetector.detect(response(403, "", "Server", "AkamaiGHost")));
    }

    @Test
    void detectsAnubisRedirect() {
        assertEquals(BotBlocker.ANUBIS, BotBlockDetector.detect(response(307, "", "Location",
                "https://example.org/.within.website/?redir=https%3A%2F%2Fexample.org")));
    }

    @Test
    void detectsAnubisChallenge() {
        assertEquals(BotBlocker.ANUBIS, BotBlockDetector.detect(response(200,
                "<script id=\"anubis_challenge\"></script>",
                "Set-Cookie", "techaro.lol-anubis-auth=challenge")));
    }

    @Test
    void detectsCloudflareChallengeHeader() {
        assertEquals(BotBlocker.CLOUDFLARE, BotBlockDetector.detect(response(403, "", "CF-Mitigated", "Challenge")));
    }

    @Test
    void detectsCloudflareBlockPage() {
        assertEquals(BotBlocker.CLOUDFLARE, BotBlockDetector.detect(response(403,
                "<h1 data-translate=\"block_headline\">Sorry, you have been blocked</h1>",
                "Server", "Cloudflare")));
    }

    @Test
    void detectsDataDome() {
        assertEquals(BotBlocker.DATADOME, BotBlockDetector.detect(response(403, "", "X-DD-B", "token",
                "X-DataDome", "protected")));
    }

    @Test
    void detectsIncapsula() {
        assertEquals(BotBlocker.INCAPSULA, BotBlockDetector.detect(response(404, "Incapsula incident ID: 123",
                "X-Iinfo", "10-123")));
    }

    @Test
    void detectsIncapsulaChallengeWrapper() {
        assertEquals(BotBlocker.INCAPSULA, BotBlockDetector.detect(response(200,
                "<iframe id=\"main-iframe\" src=\"/_Incapsula_Resource?incident_id=123\">" +
                        "Incapsula incident ID: 123</iframe>",
                "X-Iinfo", "10-123")));
    }

    @Test
    void ignoresIncapsulaChallengeWithoutIncapsulaHeader() {
        assertNull(BotBlockDetector.detect(response(200,
                "<iframe id=\"main-iframe\" src=\"/_Incapsula_Resource?incident_id=123\">" +
                        "Incapsula incident ID: 123</iframe>")));
    }

    @Test
    void ignoresOrdinaryIncapsulaResponse() {
        assertNull(BotBlockDetector.detect(response(200, "<h1>Hello</h1>", "X-Iinfo", "10-123")));
    }

    @Test
    void ignoresSignatureInNonHtmlResponse() {
        var response = new HttpResponse.Builder(403, "Forbidden")
                .addHeader("Server", "Cloudflare")
                .body(MediaType.JSON,
                        "<h1 data-translate=\"block_headline\">Sorry, you have been blocked</h1>".getBytes(UTF_8))
                .build();
        assertNull(BotBlockDetector.detect(response));
    }

    @Test
    void ignoresOrdinaryResponse() {
        assertNull(BotBlockDetector.detect(response(200, "<h1>Hello</h1>")));
    }

    private static HttpResponse response(int status, String body, String... headers) {
        var builder = new HttpResponse.Builder(status, "response");
        for (int i = 0; i < headers.length; i += 2) {
            builder.addHeader(headers[i], headers[i + 1]);
        }
        return builder.body(MediaType.HTML, body.getBytes(UTF_8)).build();
    }
}
