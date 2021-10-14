package pandas.gatherer.heritrix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "browsertrix")
public class BrowsertrixConfig {
    private String podmanOptions;

    public String getPodmanOptions() {
        return podmanOptions;
    }

    public void setPodmanOptions(String podmanOptions) {
        this.podmanOptions = podmanOptions;
    }
}
