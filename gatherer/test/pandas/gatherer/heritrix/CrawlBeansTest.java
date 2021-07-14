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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrawlBeansTest {

    @Test
    public void test(@TempDir Path jobDir) throws IOException {
        TitleGather gather = new TitleGather();
        gather.setGatherUrl("http://example.org/");
        gather.setAdditionalUrls("http://example.org/two");

        Title title = new Title();
        title.setPi(12345L);
        title.setName("test title");
        title.setGather(gather);

        Instance instance = new Instance();
        instance.setTitle(title);
        instance.setId(1L);
        instance.setDate(Instant.now());
        instance.setGatherMethodName("Heritrix");

        CrawlBeans.writeConfig(instance, jobDir, null);

        assertEquals("http://example.org/\nhttp://example.org/two\n",
                Files.readString(jobDir.resolve("seeds.txt")));

        // ensure the xmlns attribute isn't clobbered
        assertTrue(Files.readString(jobDir.resolve("crawler-beans.cxml")).contains("<beans xmlns="));
    }

}