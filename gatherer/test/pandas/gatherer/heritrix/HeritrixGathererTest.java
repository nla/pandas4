package pandas.gatherer.heritrix;

//import doss.BlobStore;
//import doss.local.LocalBlobStore;
//import org.jdbi.v3.core.Handle;
//import org.jdbi.v3.core.Jdbi;
//import org.jdbi.v3.sqlobject.SqlObjectPlugin;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TemporaryFolder;
//import org.mockito.Mockito;
//import pandas.gatherer.core.Config;
//import pandas.gatherer.core.WorkingArea;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.NoSuchAlgorithmException;
//import java.time.Instant;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.TreeMap;
//
//import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pandas.gatherer.core.Config;
import pandas.gatherer.core.WorkingArea;

import java.io.IOException;

public class HeritrixGathererTest {
//    private static Map<String, ArtifactType> artifacts = new TreeMap<>();
//
//    static {
//        artifacts.put("job.log", LOG);
//        artifacts.put("job.log.lck", null);
//        artifacts.put("crawler-beans.cxml", CONFIG);
//        artifacts.put("20170712042130/logs/runtime-errors.log.lck", null);
//        artifacts.put("20170712042130/logs/uri-errors.log.lck", null);
//        artifacts.put("20170712042130/logs/crawl.log", LOG);
//        artifacts.put("20170712042130/logs/runtime-errors.log", LOG);
//        artifacts.put("20170712042130/logs/nonfatal-errors.log.lck", null);
//        artifacts.put("20170712042130/logs/uri-errors.log", LOG);
//        artifacts.put("20170712042130/logs/frontier.recover.gz", RECOVER);
//        artifacts.put("20170712042130/logs/nonfatal-errors.log", LOG);
//        artifacts.put("20170712042130/logs/progress-statistics.log", LOG);
//        artifacts.put("20170712042130/logs/crawl.log.lck", null);
//        artifacts.put("20170712042130/logs/alerts.log", LOG);
//        artifacts.put("20170712042130/logs/progress-statistics.log.lck", null);
//        artifacts.put("20170712042130/logs/alerts.log.lck", null);
//        artifacts.put("20170712042130/job.log", LOG);
//        artifacts.put("20170712042130/job.log.lck", null);
//        artifacts.put("20170712042130/crawler-beans.cxml", CONFIG);
//        artifacts.put("20170712042130/negative-surts.dump", DUMP);
//        artifacts.put("20170712042130/surts.dump", DUMP);
//        artifacts.put("20170712042130/warcs/WEB-20170712042144216-00000-15863~hearth~8443.warc.gz", WARC);
//        artifacts.put("20170712042130/reports/source-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/processors-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/crawl-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/responsecode-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/hosts-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/frontier-summary-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/mimetype-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/threads-report.txt", REPORT);
//        artifacts.put("20170712042130/reports/seeds-report.txt", REPORT);
//        artifacts.put("state/00000000.jdb", null);
//        artifacts.put("state/je.info.0.lck", null);
//        artifacts.put("state/je.info.0", null);
//        artifacts.put("state/je.lck", null);
//        artifacts.put("scratch/tt3http.ris", null);
//        artifacts.put("scratch/tt5http.ris", null);
//    }
//
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void archive() throws IOException {
        Config config = new Config();
//        config.heritrixUrl = "http://localhost:1234";
//        config.heritrixUser = "dummy";
//        config.heritrixPassword = "dummy";
//        config.bambooSeriesId = 1L;
//        config.pywbDataDir = temp.newFolder("pywbData").toPath();

        WorkingArea workingArea = new WorkingArea(config);

//        Instance instance = new Instance(123, (long) 12345, Instant.parse("2018-06-25T03:19:56.294Z"), "Heritrix");
//
//        PandasDB mockPandasDB = Mockito.mock(PandasDB.class);
//        Mockito.when(mockPandasDB.getGatherDetailsForInstance(instance.getId()))
//                .thenReturn(new GatherDetails("Test title", "http://example.org", ""));

        HeritrixConfig heritrixConfig = new HeritrixConfig();

        HeritrixGatherer gatherer = new HeritrixGatherer(config, heritrixConfig, workingArea, null, null);

        gatherer.archive(null);

//        Path jobDir = workingArea.getInstanceDir(instance.getPi(), instance.getDateString()).resolve(instance.getHumanId());
//        Files.createDirectories(jobDir);
//
//        for (Map.Entry<String, ArtifactType> entry : artifacts.entrySet()) {
//            Path path = jobDir.resolve(entry.getKey());
//            Files.createDirectories(path.getParent());
//            Files.write(path, "Hello".getBytes(StandardCharsets.UTF_8));
//        }
//        gatherer.archive(instance);
    }
//
//    @Test
//    public void artifactType() {
//        for (Map.Entry<String, ArtifactType> entry : artifacts.entrySet()) {
//            assertEquals(entry.getKey(), entry.getValue(), HeritrixGatherer.artifactType(Paths.get(entry.getKey())));
//        }
//    }
}