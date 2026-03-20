package pandas.gatherer.heritrix;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pandas.collection.Title;
import pandas.gather.Instance;
import pandas.gather.TitleGather;
import pandas.gatherer.CrawlBeans;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CrawlBeansTest {

    @Test
    public void test(@TempDir Path jobDir) throws IOException {
        TitleGather gather = new TitleGather();
        gather.setGatherUrl("http://example.org/");
        gather.setAdditionalUrls("http://example.org/two");
        gather.setIgnoreRobotsTxt(true);

        Title title = new Title();
        title.setPi(12345L);
        title.setName("test title");
        title.setGather(gather);

        Instance instance = new Instance(title, Instant.now(), "Heritrix");

        CrawlBeans.writeConfig(instance, jobDir, null);

        assertEquals("http://example.org/\nhttp://example.org/two\n",
                Files.readString(jobDir.resolve("seeds.txt")));

        String config = Files.readString(jobDir.resolve("crawler-beans.cxml"));

        // ensure the xmlns attribute isn't clobbered
        assertTrue(config.contains("<beans xmlns="));
        assertTrue(config.contains("metadata.robotsPolicyName=ignore\n"));
    }

    @Test
    public void testGenerateAltWwwUrl() {
        assertEquals("http://www.example.com/foo.html", CrawlBeans.generateAltWwwUrl("http://example.com/foo.html"));
        assertEquals("http://example.com/foo.html", CrawlBeans.generateAltWwwUrl("http://www.example.com/foo.html"));
        assertEquals("https://www.example.com/foo.html", CrawlBeans.generateAltWwwUrl("https://example.com/foo.html"));
        assertEquals("https://example.com/foo.html", CrawlBeans.generateAltWwwUrl("https://www.example.com/foo.html"));
        assertNull(CrawlBeans.generateAltWwwUrl("ftp://example.com/foo.html"));
    }

    @Test
    public void testGenerateAltWwwSurts() {
        var seeds = List.of(
                "http://example.com/foo.html",
                "http://example.com/bar.html",
                "https://www.baz.com/");
        assertEquals(Set.of("+http://(com,baz,)/",
                        "+http://(com,example,www,)/"),
                CrawlBeans.generateAltWwwSurts(seeds));
    }
}