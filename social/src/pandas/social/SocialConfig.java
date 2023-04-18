package pandas.social;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "pandas")
public class SocialConfig {
    private Path indexDir = Path.of("data/social-index");
    private String userAgent = "pandas-social";
    private long warcSizeLimitBytes = 100 * 1024 * 1024; // 100 MB
    private long warcTimeLimitMillis = TimeUnit.HOURS.toMillis(1);

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

    public long getWarcSizeLimitBytes() {
        return warcSizeLimitBytes;
    }

    public void setWarcSizeLimitBytes(long warcSizeLimitBytes) {
        this.warcSizeLimitBytes = warcSizeLimitBytes;
    }

    public long getWarcTimeLimitMillis() {
        return warcTimeLimitMillis;
    }

    public void setWarcTimeLimitMillis(long warcTimeLimitMillis) {
        this.warcTimeLimitMillis = warcTimeLimitMillis;
    }
}
