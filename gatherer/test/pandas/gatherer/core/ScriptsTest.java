package pandas.gatherer.core;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pandas.gather.Instance;
import pandas.gatherer.httrack.HttrackUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class ScriptsTest {
    static final int pi = 12345;
    static final String dateString = "20170101-1234";

    @TempDir Path master;
    @TempDir Path workingDir;
    @TempDir Path derivative;

    private Config buildTestConfig() throws IOException {
        Path warcDir = master.resolve("warc");
        Files.createDirectory(warcDir);

        Config config = new Config();
        config.setScriptsDir(Paths.get("../PandasPerlScripts"));
        Files.createDirectory(workingDir.resolve("mime"));
        config.setWorkingDir(workingDir);
        config.setMastersDir(master);
        config.setRepo1Dir(derivative);
        config.setRepo2Dir(warcDir);

        Files.createDirectory(workingDir.resolve("../upload"));
        Files.createFile(workingDir.resolve("../upload/exclude-files.lst"));
        Files.createDirectory(workingDir.resolve("../warc"));
        Files.createDirectories(master.resolve("access/arc3"));
        Files.createDirectories(master.resolve("mime/arc3"));
        Files.createDirectories(master.resolve("preserve/arc3"));

        return config;
    }

    Config config;
    Scripts scripts;

    @BeforeEach
    public void setUp() throws IOException {
        config = buildTestConfig();
        scripts = new Scripts(config);
    }

    String htmeta = "HTTP/1.1 302 Found\r\n" +
            "X-In-Cache: 1\r\n" +
            "X-StatusCode: 200\r\n" +
            "X-StatusMessage: Found\r\n" +
            "Content-Type: magic/pony\r\n" +
            "Last-Modified: Sat, 13 May 2017 14:02:15 GMT\r\n" +
            "Location: http://example.org/\r\n" +
            "X-Addr: example.org\r\n" +
            "X-Fil: /\r\n" +
            "X-Save: example.org/index.html\r\n";

    @Test
    public void testPostGather() throws Exception {
        Path instanceDir = config.getWorkingDir().resolve("" + pi).resolve(dateString);
        Path siteDir = instanceDir.resolve("example.org");
        populateTestInstance(instanceDir);

        HttrackUtils.postGather(pi, dateString, instanceDir);

        String urlMap = new String(Files.readAllBytes(instanceDir.resolve("url.map")));
        assertEquals("http://example.org/index.html^^12345/20170101-1234/example.org/index.html\n", urlMap);

        assertEquals("<Files \"index.html\">\n" +
                "\tForceType magic/pony\n" +
                "</Files>\n" +
                "\n", new String(Files.readAllBytes(siteDir.resolve(".panaccess-mime.types"))));

        assertEquals("Hello world <a href=\"/external.html\">test</a>",
                new String(Files.readAllBytes(siteDir.resolve("index.html"))));
    }

    @Test
    public void testArchivePreserve() throws IOException, InterruptedException {
        Path instanceDir = config.getWorkingDir().resolve("" + pi).resolve(dateString);
        populateTestInstance(instanceDir);

        Instance instance = Instance.createDummy(pi, dateString);

        new WorkingArea(config, new ClassicRepository(config)).preserveInstance(instance);

        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.md5")));
        assertTrue(Files.readString(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.md5"))
                .matches("[0-9a-f]{32} {2}.*/upload/ps-ar2-12345-20170101-1234\\.tgz\n"));
        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.tgz")));
        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.sz")));
        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.lst")));
    }

    private void populateTestInstance(Path instanceDir) throws IOException {
        Path siteDir = instanceDir.resolve("example.org");
        Files.createDirectories(siteDir);
        Files.write(siteDir.resolve("index.html"), "Hello world <a href=\"../../external.html\">test</a>".getBytes());

        Path htsCacheDir = instanceDir.resolve("hts-cache");
        Files.createDirectory(htsCacheDir);
        Files.write(htsCacheDir.resolve("doit.log"), ("-%H http://example.org/\n" +
                "File generated automatically on Wed, 25 Oct 2017 18:41:47, do NOT edit\n" +
                "\n" +
                "To update a mirror, just launch httrack without any parameters\n" +
                "The existing cache will be used (and modified)\n" +
                "To have other options, retype all parameters and launch HTTrack\n" +
                "To continue an interrupted mirror, just launch httrack without any parameters\n" +
                "\n").getBytes(StandardCharsets.UTF_8));
        Files.write(htsCacheDir.resolve("new.txt"),
                ("date\tsize'/'remotesize\tflags(request:Update,Range state:File response:Modified,Chunked,gZipped)\tstatuscode\tstatus ('servermsg')\tMIME\tEtag|Date\tURL\tlocalfile\t(from URL)\n" +
                        "18:41:48\t169/169\t---M--\t404\terror ('Not%20Found')\ttext/html\tdate:Wed,%2025%20Oct%202017%2009:41:47%20GMT\thttp://example.org/robots.txt\t\t(from )\n" +
                        "18:41:48\t219/219\t---M--\t200\tadded ('OK')\ttext/html\tetag:%2259f05c4e-db%22\thttp://example.org/index.html\texample.org/index.html\t(from )\n").getBytes());

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(htsCacheDir.resolve("new.zip")))) {
            ZipEntry entry = new ZipEntry("http://example.org/index.html");
            entry.setExtra(htmeta.getBytes());
            zos.putNextEntry(entry);
            zos.closeEntry();
            zos.finish();
            zos.flush();
        }
    }

}
