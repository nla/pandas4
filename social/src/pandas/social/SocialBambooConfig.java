package pandas.social;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "bamboo")
@Validated
public class SocialBambooConfig {
    @NotNull
    private String url;
    private long collectionId;
    private Long crawlSeriesId;
    private Long attachmentCrawlSeriesId;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public Long getCrawlSeriesId() {
        return crawlSeriesId;
    }

    public void setCrawlSeriesId(Long crawlSeriesId) {
        this.crawlSeriesId = crawlSeriesId;
    }

    public Long getAttachmentCrawlSeriesId() {
        return attachmentCrawlSeriesId;
    }

    public void setAttachmentCrawlSeriesId(Long attachmentCrawlSeriesId) {
        this.attachmentCrawlSeriesId = attachmentCrawlSeriesId;
    }
}
