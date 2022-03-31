package pandas.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netpreserve.jwarc.*;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.netpreserve.jwarc.WarcCompression.GZIP;

public class FileSeacherTest {
    @TempDir
    Path warcDir;

    @TempDir
    Path indexDir;

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        Instant date = Instant.parse("2020-08-30T12:34:56Z");
        byte[] html = "<h1>Hello world</h1>".getBytes(UTF_8);
        try (var writer = new WarcWriter(FileChannel.open(warcDir.resolve("test.warc.gz"), WRITE, CREATE), GZIP)) {
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
            writer.write(response);
        }

        var index = new FileSeacher(indexDir);
        index.indexRecursively(warcDir);
        LinkedMultiValueMap<String, String> filters = new LinkedMultiValueMap<>();
        filters.add("status", "200");
        var results = index.search("", filters);
        assertEquals(1, results.list().size());
        assertEquals(new FileSeacher.Result(date, "http://example.org/", 200, (long) html.length, "text/html",
                "NNO3EJ5GVIRCRR3XWB3RCCFRQSY7YXPT"), results.list().get(0));
    }

}