package pandas.gatherer.heritrix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bamboo")
public class BambooConfig {
    /**
     * URL of the Bamboo server.
     */
    private String url;

    /**
     * ID of the crawl series to deposit PANDAS crawls into.
     */
    private Long crawlSeriesId;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getCrawlSeriesId() {
        return crawlSeriesId;
    }

    public void setCrawlSeriesId(Long crawlSeriesId) {
        this.crawlSeriesId = crawlSeriesId;
    }
}
