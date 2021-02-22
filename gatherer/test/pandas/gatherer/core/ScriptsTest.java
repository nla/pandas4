package pandas.gatherer.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class ScriptsTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    static final int pi = 12345;
    static final String dateString = "20170101-1234";

    private Config buildTestConfig() throws IOException {

        Path master = tmp.newFolder("master").toPath();
        Path warcDir = master.resolve("warc");
        Files.createDirectory(warcDir);

        Config config = new Config();
        config.setScriptsDir(Paths.get("../PandasPerlScripts"));
        Path workingDir = tmp.newFolder("working").toPath();
        Files.createDirectory(workingDir.resolve("mime"));
        config.setWorkingDir(workingDir);
        config.setMastersDir(master);
        config.setRepo1Dir(tmp.newFolder("derivative").toPath());
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

    @Before
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

        scripts.postGather(pi, dateString);

        String urlMap = new String(Files.readAllBytes(instanceDir.resolve("url.map")));
        assertEquals("http://example.org/index.html^^12345/20170101-1234/example.org/index.html\n", urlMap);

        assertEquals("<Files \"index.html\">\n" +
                "\tForceType magic/pony\n" +
                "</Files>\n" +
                "\n", new String(Files.readAllBytes(siteDir.resolve(".panaccess-mime.types"))));

        assertEquals("Hello world <a href=\"/external.html\">test</a>",
                new String(Files.readAllBytes(siteDir.resolve("index.html"))));

        assertEquals(htmeta.replace("\r\n", "\n"), new String(Files.readAllBytes(config.getWorkingDir().resolve("mime/12345/20170101-1234/example.org/index.html"))));

    }

    @Test
    public void testArchiveMove() throws IOException, InterruptedException {
        Path instanceDir = config.getWorkingDir().resolve("" + pi).resolve(dateString);
        populateTestInstance(instanceDir);

        new WorkingArea(config).archiveInstance(pi, dateString);

        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20170101-1234.md5")));
        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20170101-1234.tgz")));
        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20170101-1234.lst")));
        assertTrue(Files.exists(config.getMastersDir().resolve("access/arc3/012/12345/ac-ar2-12345-20170101-1234.sz")));
//        if (!System.getenv().containsKey("PANDORA2WARC_JAR")) {
//            assertTrue(Files.exists(config.getMastersDir().resolve("warc/012/12345/nla.arc-12345-20170101-1234.cdx")));
//            assertTrue(Files.exists(config.getMastersDir().resolve("warc/012/12345/nla.arc-12345-20170101-1234-0.warc.gz")));
//            assertTrue(Files.exists(config.getRepo1Dir().resolve("012/12345/nla.arc-12345-20170101-1234.cdx")));
//            assertTrue(Files.exists(config.getRepo1Dir().resolve("012/12345/nla.arc-12345-20170101-1234-0.warc.gz")));
//        }

        find();
    }

    private void find() throws InterruptedException, IOException {
        //new ProcessBuilder("find", config.getWorkingDir().getParent().toString()).redirectOutput(ProcessBuilder.Redirect.INHERIT).start().waitFor();
    }

    @Test
    public void testUploadProcess() throws IOException, InterruptedException {
        Path instanceDir = config.getWorkingDir().resolve("" + pi).resolve(dateString);
        populateTestInstance(instanceDir);

        scripts.uploadProcess(pi, dateString);

        assertTrue(Files.exists(config.getWorkingDir().resolve("12345/20170101-1234/example.org/.panaccess-mime.types")));
    }

    @Test
    public void testArchivePreserve() throws IOException, InterruptedException {
        Path instanceDir = config.getWorkingDir().resolve("" + pi).resolve(dateString);
        populateTestInstance(instanceDir);

        new WorkingArea(config).preserveInstance(pi, dateString);

        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.md5")));
        assertTrue(Files.readString(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.md5"))
                .matches("[0-9a-f]{32} {2}.*/upload/ps-ar2-12345-20170101-1234\\.tgz\n"));
        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.tgz")));
        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.sz")));
        assertTrue(Files.exists(config.getMastersDir().resolve("preserve/arc3/012/12345/ps-ar2-12345-20170101-1234.lst")));

        find();
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
