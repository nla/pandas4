package pandas.gatherer.heritrix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "browsertrix")
public class BrowsertrixConfig {
    private String podmanOptions;
    private int pageLimit = 1000;
    private String userAgentSuffix = "nla.gov.au_bot (National Library of Australia Legal Deposit Request; +http://www.nla.gov.au/legal-deposit/request)";

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
}