package pandas.gatherer.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.MediaType;
import org.mockserver.springtest.MockServerTest;
import pandas.gather.InstanceThumbnail;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@MockServerTest
@ExtendWith(MockServerExtension.class)
class ThumbnailGeneratorTest {
    private final ClientAndServer mockServer;

    ThumbnailGeneratorTest(ClientAndServer mockServer) {
        this.mockServer = mockServer;
    }

    @Test
    public void html() throws Exception {
        mockServer.when(request("/test.html")).respond(response("<h1>Hello world</h1>"));
        ThumbnailGenerator generator = new ThumbnailGenerator(null, null, null);
        InstanceThumbnail thumbnail = generator.generateThumbnail("http://127.0.0.1:" + mockServer.getPort() + "/test.html");
        assertEquals(200, thumbnail.getStatus());
        assertNotNull(thumbnail.getData());
        assertTrue(thumbnail.getData().length > 0);
    }

    @Test
    public void pdf() throws Exception {
        byte[] testPdf;
        try (InputStream stream = getClass().getResourceAsStream("test.pdf")) {
            testPdf = requireNonNull(stream).readAllBytes();
        }
        mockServer.when(request("/test.pdf")).respond(response().withBody(testPdf).withContentType(MediaType.PDF));
        ThumbnailGenerator generator = new ThumbnailGenerator(null, null, null);
        InstanceThumbnail thumbnail = generator.generateThumbnail("http://127.0.0.1:" + mockServer.getPort() + "/test.pdf");
        assertEquals(200, thumbnail.getStatus());
        assertNotNull(thumbnail.getData());
        assertTrue(thumbnail.getData().length > 0);
        Files.write(Paths.get("/tmp/x.pdf"), thumbnail.getData());
    }

}