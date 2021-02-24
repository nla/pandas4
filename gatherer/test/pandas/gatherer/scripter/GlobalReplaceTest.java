package pandas.gatherer.scripter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class GlobalReplaceTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        Path root = folder.newFolder().toPath();
        Files.writeString(root.resolve("foo.html"), "foofod");
        Files.writeString(root.resolve("foo.txt"), "foofoo");
        Files.writeString(root.resolve("baz.html"), "bazbaz");
        Path subdir = root.resolve("subdir");
        Files.createDirectory(subdir);
        Files.writeString(subdir.resolve("subdir.html"), "foofoo");

        StringBuilder log = new StringBuilder();
        var report = GlobalReplace.globrep(root, 1, "*.html", Pattern.compile("fo[od]"), "bar", log);
        assertEquals("foo.html matched with 2 changes\n", log.toString());
        assertEquals("barbar", Files.readString(root.resolve("foo.html")));
        assertEquals("foofoo", Files.readString(root.resolve("foo.txt")));
        assertEquals("bazbaz", Files.readString(root.resolve("baz.html")));
        assertEquals("foofoo", Files.readString(subdir.resolve("subdir.html")));
        assertEquals(1, report.directoriesProcessed);
        assertEquals(1, report.filesChanged);
        assertEquals(2, report.filesProcessed);
        assertEquals(2, report.substitutionsMade);

        var report2 = GlobalReplace.globrep(root, Integer.MAX_VALUE, "*.html", Pattern.compile("fo[od]"), "bar", log);
        assertEquals("barbar", Files.readString(subdir.resolve("subdir.html")));
        assertEquals(2, report2.directoriesProcessed);
    }
}