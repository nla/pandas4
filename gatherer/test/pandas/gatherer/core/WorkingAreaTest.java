package pandas.gatherer.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pandas.gather.Instance;
import pandas.gatherer.repository.ClassicRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class WorkingAreaTest {
    Path workingDir;
    Path instanceDir;
    WorkingArea workingArea;

    long pi = 12345;
    String date = "20210223-0954";
    private Config config;

    @BeforeEach
    public void setUp(@TempDir Path temp) throws IOException {
        workingDir = Files.createDirectories(temp.resolve("working"));
        instanceDir = workingDir.resolve(Long.toString(pi)).resolve(date);
        config = new Config();
        config.setWorkingDir(workingDir);
        config.setMastersDir(Files.createDirectories(temp.resolve("master")));
        config.setRepo1Dir(Files.createDirectories(temp.resolve("repo1")));
        config.setRepo2Dir(Files.createDirectories(temp.resolve("repo2")));
        config.setUploadDir(Files.createDirectories(temp.resolve("upload")));
        workingArea = new WorkingArea(config, new ClassicRepository(config));
    }

    @Test
    public void testCreateInstance() throws IOException {
        workingArea.createInstance(pi, date);
        assertTrue(Files.isDirectory(instanceDir));
    }

    @Test
    public void testArchiveInstance() throws IOException, InterruptedException {
        TestUtils.unzip(workingDir, "/pandas/gatherer/httrack/testcrawl.zip");

        workingArea.archiveInstance(Instance.createDummy(pi, date));

        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20210223-0954.md5")));
        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20210223-0954.tgz")));
        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20210223-0954.lst")));
        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20210223-0954.sz")));
//        if (!System.getenv().containsKey("PANDORA2WARC_JAR")) {
//            assertTrue(Files.exists(config.getMastersDir().resolve("warc/012/12345/nla.arc-12345-20170101-1234.cdx")));
//            assertTrue(Files.exists(config.getMastersDir().resolve("warc/012/12345/nla.arc-12345-20170101-1234-0.warc.gz")));
//            assertTrue(Files.exists(config.getRepo1Dir().resolve("012/12345/nla.arc-12345-20170101-1234.cdx")));
//            assertTrue(Files.exists(config.getRepo1Dir().resolve("012/12345/nla.arc-12345-20170101-1234-0.warc.gz")));
//        }
    }


    @Test
    public void testDeleteInstance() throws IOException {
        Files.createDirectories(instanceDir);
        Files.write(instanceDir.resolve("test.txt"), "Hello".getBytes());
        Path mimeDir = workingDir.resolve("mime").resolve(Long.toString(pi)).resolve(date);
        Files.createDirectories(mimeDir);
        Files.createDirectory(mimeDir.resolve("www.example.org"));
        Files.write(mimeDir.resolve("test.file"), "hello".getBytes());
        Files.setPosixFilePermissions(instanceDir.resolve("test.txt"), Collections.emptySet());
        Files.setPosixFilePermissions(instanceDir, Collections.emptySet());
        workingArea.deleteInstance(pi, date);
        assertFalse(Files.exists(instanceDir));
    }

    @Test
    public void testDeleteInstanceShouldCleanupEmptyTitle() throws IOException {
        workingArea.createInstance(pi, date);
        workingArea.createInstance(pi, "20170202-4321");

        workingArea.deleteInstance(pi, date);
        Path titleDir = instanceDir.getParent();
        assertTrue(Files.exists(titleDir));
        workingArea.deleteInstance(pi, "20170202-4321");
        assertFalse(Files.exists(titleDir));
    }

    @Test
    public void testInstanceStats() throws IOException {
        workingArea.createInstance(pi, date);
        Files.write(instanceDir.resolve("hello.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
        Files.createDirectory(instanceDir.resolve("subdir"));
        {
            FileStats stats = workingArea.instanceStats(pi, date, () -> false);
            assertEquals(1, stats.fileCount());
            assertEquals(5, stats.size());
        }
        Files.write(instanceDir.resolve("subdir").resolve("hello2.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
        {
            FileStats stats = workingArea.instanceStats(pi, date, () -> false);
            assertEquals(2, stats.fileCount());
            assertEquals(10, stats.size());
        }
    }
}
