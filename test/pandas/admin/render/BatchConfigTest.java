package pandas.admin.render;

import org.junit.Test;
import pandas.admin.collection.Title;

import static org.junit.jupiter.api.Assertions.*;

public class BatchConfigTest {
    @Test
    public void test() throws Exception {
        var processor = new BatchConfig.Processor();
        processor.open(null);
        Title title = new Title();
        title.setTitleUrl("http://example.org/");
        byte[] data = processor.process(title);
        assertNotNull(data);
    }
}