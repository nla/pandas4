package pandas;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "pandas")
public class PandasSocialConfig {
    private Path indexDir = Path.of("data/social-index");

    public Path getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(Path indexDir) {
        this.indexDir = indexDir;
    }
}
