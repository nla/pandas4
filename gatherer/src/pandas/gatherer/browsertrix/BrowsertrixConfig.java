package pandas.gatherer.browsertrix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "browsertrix")
public class BrowsertrixConfig {
    private String podmanOptions;
    private String userAgentSuffix = "nla.gov.au_bot (National Library of Australia Legal Deposit Request; +http://www.nla.gov.au/legal-deposit/request)";
    private int workers = 4;

    private Long defaultCrawlLimitBytes = 2L * 1024L * 1024L * 1024L;

    private Long defaultCrawlLimitSeconds = 12L * 60 * 60;
    private String image = "webrecorder/browsertrix-crawler:1.11.2";

    private boolean kubeEnabled = false;
    private String kubeNamespace;
    private Path kubeJobConfig;

    public String getPodmanOptions() {
        return podmanOptions;
    }

    public void setPodmanOptions(String podmanOptions) {
        this.podmanOptions = podmanOptions;
    }

    public String getUserAgentSuffix() {
        return userAgentSuffix;
    }

    public void setUserAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public void setDefaultCrawlLimitBytes(Long defaultCrawlLimitBytes) {
        this.defaultCrawlLimitBytes = defaultCrawlLimitBytes;
    }

    public Long getDefaultCrawlLimitBytes() {
        return defaultCrawlLimitBytes;
    }

    public Long getDefaultCrawlLimitSeconds() {
        return defaultCrawlLimitSeconds;
    }

    public void setDefaultCrawlLimitSeconds(Long defaultCrawlLimitSeconds) {
        this.defaultCrawlLimitSeconds = defaultCrawlLimitSeconds;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isKubeEnabled() {
        return kubeEnabled;
    }

    public void setKubeEnabled(boolean kubeEnabled) {
        this.kubeEnabled = kubeEnabled;
    }

    public String getKubeNamespace() {
        return kubeNamespace;
    }

    public void setKubeNamespace(String kubeNamespace) {
        this.kubeNamespace = kubeNamespace;
    }

    public Path getKubeJobConfig() {
        return kubeJobConfig;
    }

    public void setKubeJobConfig(Path kubeJobConfig) {
        this.kubeJobConfig = kubeJobConfig;
    }
}
