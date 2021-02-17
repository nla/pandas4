package pandas.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pandas")
public class Config {
    private String bambooUrl = "https://pandas.nla.gov.au/bamboo";
    private String managementUrl;
    private String autologin;

    public String getAutologin() {
        return autologin;
    }

    public void setAutologin(String autologin) {
        this.autologin = autologin;
    }

    public String getBambooUrl() {
        return bambooUrl;
    }

    public void setBambooUrl(String bambooUrl) {
        this.bambooUrl = bambooUrl;
    }

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
