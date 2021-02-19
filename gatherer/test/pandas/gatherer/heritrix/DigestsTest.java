package pandas.gatherer.heritrix;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class DigestsTest {
    @Test
    public void sha256() throws IOException, NoSuchAlgorithmException {
        Path tmp = Files.createTempFile("pandas", ".tmp");
        try {
            Files.write(tmp, "Hello world".getBytes(StandardCharsets.UTF_8));
            assertEquals("64ec88ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c",
                    Digests.sha256(tmp));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}