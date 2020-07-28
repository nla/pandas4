package pandas.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pandas")
public class Config {
    private String managementUrl;

    public String getManagementUrl() {
        return managementUrl;
    }

    public void setManagementUrl(String managementUrl) {
        this.managementUrl = managementUrl;
    }

    public String managementDirectActionUrl(String action) {
        return managementUrl + "/WebObjects/PandasManagement.woa/wa/" + action;
    }
}
