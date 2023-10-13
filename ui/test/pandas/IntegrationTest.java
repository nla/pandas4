package pandas;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ContextConfiguration(initializers = IntegrationTest.Initializer.class)
public abstract class IntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    public static Path tempDir;

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
                LoggerFactory.getLogger(IntegrationTest.class).warn("Failed to cleanup temp dir {}", tempDir, e);
            }
        }));
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("DATA_PATH=" + tempDir + "/data").applyTo(context);
        }
    }
}
