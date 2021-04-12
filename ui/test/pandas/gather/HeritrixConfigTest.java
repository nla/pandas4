package pandas.gather;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class HeritrixConfigTest {

    @Test
    public void beansXml() throws IOException {
        HeritrixConfig config = new HeritrixConfig();
        config.jobName = "test";
        config.description = "test";
        config.surts = List.of("http://example.com/");
        config.seeds = List.of("http://example.com/");
        System.out.println(config.beansXml());
        Files.write(Paths.get("/tmp/foo.xml"), config.beansXml().getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }
}