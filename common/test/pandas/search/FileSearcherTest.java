package pandas.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netpreserve.jwarc.*;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.netpreserve.jwarc.WarcCompression.GZIP;

public class FileSearcherTest {
    @TempDir
    Path warcDir;

    @TempDir
    Path indexDir;

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        Instant date = Instant.parse("2020-08-30T12:34:56Z");
        byte[] html = "<h1>Hello world</h1>".getBytes(UTF_8);

        var http = new HttpResponse.Builder(200, "OK")
                .body(MediaType.HTML, html)
                .build();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(html);
        var response = new WarcResponse.Builder("http://example.org/")
                .date(date)
                .body(http)
                .payloadDigest(new WarcDigest(digest))
                .build();

        var httpRequest = new HttpRequest.Builder("POST", "/")
                .build();
        var request = new WarcRequest.Builder("http://example.org/")
                .concurrentTo(response.id())
                .date(date)
                .body(httpRequest)
                .build();

        long requestOffset;
        try (var writer = new WarcWriter(FileChannel.open(warcDir.resolve("test.warc.gz"), WRITE, CREATE), GZIP)) {
            writer.write(response);
            requestOffset = writer.position();
            writer.write(request);
        }

        var index = new FileSearcher(indexDir);
        index.indexRecursively(warcDir);
        LinkedMultiValueMap<String, String> filters = new LinkedMultiValueMap<>();
        filters.add("status", "200");
        var results = index.search("", filters, Pageable.ofSize(100));
        assertEquals(1, results.getNumberOfElements());
        FileSearcher.Result result = results.toList().get(0);
        assertEquals(new FileSearcher.Result(response.id().toString().replaceFirst("^urn:uuid:", ""),
                        date, "POST", "http://example.org/",
                        200, (long) html.length, "html", "NNO3EJ5GVIRCRR3XWB3RCCFRQSY7YXPT",
                        requestOffset, 0L, null, Paths.get("test.warc.gz")),
                result);
    }

}