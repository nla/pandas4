package pandas.gather;

import org.junit.Test;

import java.util.List;

public class HeritrixConfigTest {

    @Test
    public void beansXml() {
        HeritrixConfig config = new HeritrixConfig();
        config.jobName = "test";
        config.description = "test";
        config.surts = List.of("http://example.com/");
        config.seeds = List.of("http://example.com/");
        System.out.println(config.beansXml());
    }
}