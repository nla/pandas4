package pandas.gatherer.core;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pandas.collection.Title;
import pandas.gather.Instance;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PywbServiceTest {
    @TempDir
    Path dir;

    @Test
    public void test() throws Exception {
        Config config = new Config();
        config.setPywbDataDir(dir);
        PywbConfig pywbConfig = new PywbConfig();
        PywbService pywbService;
        try {
            pywbService = new PywbService(config, pywbConfig);
        } catch (IOException e) {
            Assumptions.assumeTrue(false, "Unable to start pywb");
            return;
        }
        Assumptions.assumeTrue(pywbService.isAvailable(), "pywb not available");
        try {
            Title title = new Title();
            title.setPi(123L);
            Instance instance = new Instance(title, Instant.parse("1987-08-30T12:00:00Z"), "Heritrix");
            instance.setTepUrl("http://example.org/");
            String url = pywbService.replayUrlFor(instance);
            var connection = (HttpURLConnection) new URL(url).openConnection();
            try {
                // there's no data there but make sure it actually responds
                assertEquals(404, connection.getResponseCode());
            } finally {
                connection.disconnect();
            }
        } finally {
            pywbService.destroy();
        }
    }

}