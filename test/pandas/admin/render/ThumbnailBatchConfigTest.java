package pandas.admin.render;

import org.junit.Test;
import pandas.admin.collection.Thumbnail;
import pandas.admin.collection.ThumbnailProcessor;
import pandas.admin.collection.Title;

import static org.junit.jupiter.api.Assertions.*;

public class ThumbnailBatchConfigTest {
    @Test
    public void test() throws Exception {
        var processor = new ThumbnailProcessor();
        processor.open(null);
        Title title = new Title();
        title.setTitleUrl("http://example.org/");
        Thumbnail thumbnail = processor.process(title);
        assertNotNull(thumbnail.getData());
    }
}