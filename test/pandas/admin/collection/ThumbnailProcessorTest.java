package pandas.admin.collection;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ThumbnailProcessorTest {
    @Test
    @Ignore
    public void test() throws Exception {
        var processor = new ThumbnailProcessor();
        processor.open(null);
        try {
            Title title = new Title();
            title.setTitleUrl("http://miff.com.au/");
            Thumbnail thumbnail = processor.process(title);
            assertNotNull(thumbnail.getData());
            Files.write(Paths.get("/tmp/thumb.jpg"), thumbnail.getData());
        } finally {
            processor.close();
        }
    }
}