package pandas.crawlconfig;

import java.util.List;

public class CrawlConfig {
    private String id;

    private List<Seed> seeds;

    private String userAgent;

    public CrawlConfig(String id, List<Seed> seeds) {
        this.id = id;
        this.seeds = seeds;
    }

    public List<Seed> getSeeds() {
        return seeds;
    }

    public String getId() {
        return id;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
