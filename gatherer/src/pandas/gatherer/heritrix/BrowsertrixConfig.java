package pandas.gatherer.heritrix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "browsertrix")
public class BrowsertrixConfig {
    private String podmanOptions;
    private int pageLimit = 1000;
    private String userAgentSuffix = "nla.gov.au_bot (National Library of Australia Legal Deposit Request; +http://www.nla.gov.au/legal-deposit/request)";
    private int workers = 4;

    private Long defaultCrawlLimitBytes = 2L * 1024L * 1024L * 1024L;

    private Long defaultCrawlLimitSeconds = 12L * 60 * 60;
    private String version = "webrecorder/browsertrix-crawler:1.3.5";

    public String getPodmanOptions() {
        return podmanOptions;
    }

    public void setPodmanOptions(String podmanOptions) {
        this.podmanOptions = podmanOptions;
    }

    public int getPageLimit() {
        return pageLimit;
    }

    public void setPageLimit(int pageLimit) {
        this.pageLimit = pageLimit;
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
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
