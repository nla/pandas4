package pandas.core;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This initializer sets the DATA_PATH property to a temporary directory, and deletes it when the JVM exits.
 * This is used for integration tests because Hibernate Search needs somewhere to write to. We can't use
 * junit's @TempDir because it cleans up before Hibernate Search is finished with it.
 */
public class TempDataPathInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(TempDataPathInitializer.class);
    private static Path tempDir;

    static {
        try {
            tempDir = Files.createTempDirectory("pandas-test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                log.warn("Failed to cleanup temp dir {}", tempDir, e);
            }
        }));
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of("DATA_PATH=" + tempDir + "/data").applyTo(context);
    }
}
