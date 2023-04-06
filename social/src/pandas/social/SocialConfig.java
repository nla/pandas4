package pandas.social;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "pandas")
public class SocialConfig {
    private Path indexDir = Path.of("data/social-index");
    private String userAgent = "pandas-social";

    public Path getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(Path indexDir) {
        this.indexDir = indexDir;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
