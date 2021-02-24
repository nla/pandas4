package pandas.gatherer.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestUtils {
    @SuppressWarnings("SameParameterValue")
    public static void unzip(Path root, String resource) throws IOException {
        InputStream stream = TestUtils.class.getResourceAsStream(resource);
        if (stream == null) throw new RuntimeException("missing resource: " + resource);
        try (ZipInputStream zis = new ZipInputStream(stream)) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                Path path = root.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(path);
                } else {
                    Files.copy(zis, path);
                }
            }
        }
    }
}
