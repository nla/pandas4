package pandas.gatherer.core;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pandas.browser.BrowserMissingException;
import pandas.gather.InstanceThumbnail;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ThumbnailGeneratorTest {
    private static HttpServer server;

    @BeforeAll
    public static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        // /test.html → always return HTML
        server.createContext("/test.html", exchange -> {
            byte[] body = "<h1>Hello world</h1>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            exchange.close();
        });

        server.createContext("/test.pdf", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/pdf");
            exchange.sendResponseHeaders(200, 0);
            try (var output = exchange.getResponseBody();
                 var input = ThumbnailGeneratorTest.class.getResourceAsStream("test.pdf")) {
                input.transferTo(output);
            }
            exchange.close();
        });

        server.start();
    }

    @AfterAll
    public static void tearDown() {
        if (server != null) server.stop(0);
    }


    @Test
    public void html() throws Exception {
        try {
            ThumbnailGenerator generator = new ThumbnailGenerator(null, null, null);
            InstanceThumbnail thumbnail = generator.generateThumbnail("http://127.0.0.1:" + server.getAddress().getPort() + "/test.html");
            assertEquals(200, thumbnail.getStatus());
            assertNotNull(thumbnail.getData());
            assertTrue(thumbnail.getData().length > 0);
        } catch (BrowserMissingException e) {
            assumeTrue(false, "Browser not installed: " + e);
        }
    }

    @Test
    public void pdf() throws Exception {
        try {
            new ProcessBuilder("gs", "--version")
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start().waitFor();
        } catch (IOException e) {
            Assumptions.abort("Failed to run gs");
        }
        ThumbnailGenerator generator = new ThumbnailGenerator(null, null, null);
        InstanceThumbnail thumbnail = generator.generateThumbnail("http://127.0.0.1:" + server.getAddress().getPort() + "/test.pdf");
        assertEquals(200, thumbnail.getStatus());
        assertNotNull(thumbnail.getData());
        assertTrue(thumbnail.getData().length > 0);
    }

}