package pandas.gatherer.httrack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcRecord;
import org.netpreserve.jwarc.WarcResource;
import pandas.gatherer.core.TestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Pandora2WarcTest {
    @Test
    public void test(@TempDir Path destDir, @TempDir Path srcDir) throws IOException {
        Path root = srcDir.resolve("12345").resolve("20210223-0954");
        TestUtils.unzip(srcDir, "/pandas/gatherer/httrack/testcrawl.zip");
        Pandora2Warc.convertInstance(root, destDir);
        Path warc = destDir.resolve("nla.arc-12345-20210223-0954-000.warc.gz");
        assertTrue(Files.exists(warc));

        Set<String> seen = new HashSet<>();
        try (WarcReader reader = new WarcReader(warc)) {
            for (WarcRecord record: reader) {
                WarcResource resource = (WarcResource) record;
                seen.add(resource.target());
                if (resource.target().endsWith("/index.css")) {
                    assertEquals("text/css", resource.contentType().toString());
                    assertEquals("  .foo { background: url(green.png); }\n", new String(record.body().stream().readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }
        assertEquals(Set.of("http://pandora.nla.gov.au/pan/12345/20210223-0954/hts-cache/new.zip",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/hts-cache/new.txt",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/hts-cache/doit.log",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/hts-cache/new.lst",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/hts-log.txt",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/127.0.0.1/blue.png",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/127.0.0.1/index.css",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/127.0.0.1/index.html",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/127.0.0.1/two.html",
                "http://pandora.nla.gov.au/pan/12345/20210223-0954/127.0.0.1/green.png"), seen);
    }
}