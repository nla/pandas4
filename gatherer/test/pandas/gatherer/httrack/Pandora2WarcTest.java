package pandas.gatherer.httrack;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcRecord;
import org.netpreserve.jwarc.WarcResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Pandora2WarcTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        Path destDir = folder.newFolder().toPath();
        Path srcDir = folder.newFolder().toPath();
        Path root = srcDir.resolve("12345").resolve("20210223-0954");
        unzip(srcDir, "testcrawl.zip");
        Pandora2Warc.convertInstance(root, destDir);
        Path warc = destDir.resolve("nla.arc-12345-20210223-0954.warc");
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

    @SuppressWarnings("SameParameterValue")
    private void unzip(Path root, String resource) throws IOException {
        InputStream stream = getClass().getResourceAsStream(resource);
        if (stream == null) throw new RuntimeException("missing resource: " + resource);
        try (ZipInputStream zis = new ZipInputStream(stream)) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                Path path = root.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(path);
                } else {
                    Files.copy(zis, path);
                }
            }
        }
    }
}