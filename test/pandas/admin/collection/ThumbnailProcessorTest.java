package pandas.admin.collection;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ThumbnailProcessorTest {
    @Test
    @Ignore
    public void test() throws Exception {
        var processor = new ThumbnailProcessor();
        processor.open(null);
        try {
            Title title = new Title();
            title.setTitleUrl("http://example.org/");
            Thumbnail thumbnail = processor.process(title);
            assertNotNull(thumbnail.getData());
        } finally {
            processor.close();
        }
    }
}