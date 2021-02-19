package pandas.gatherer.core;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.*;

public class WorkingAreaTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    Path workingDir, instanceDir;
    WorkingArea workingArea;

    long pi = 12345;
    String date = "20120202-1234";

    @Before
    public void setUp() throws IOException {
        workingDir = temp.newFolder("working").toPath();
        instanceDir = workingDir.resolve(Long.toString(pi)).resolve(date);
        Config config = new Config();
        config.setWorkingDir(workingDir);
        workingArea = new WorkingArea(config);
    }

    @Test
    public void testCreateInstance() throws IOException {
        workingArea.createInstance(pi, date);
        assertTrue(Files.isDirectory(instanceDir));
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
