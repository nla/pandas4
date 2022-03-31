package pandas.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pandas.gather.Instance;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "pandas")
public class Config {
    private String bambooUrl = "https://pandas.nla.gov.au/bamboo";
    private String managementUrl;
    private String autologin;
    private String workingAreaUrl = "https://pandas.nla.gov.au/view/";
    private String qaReplayUrl = "https://pwb.archive.org.au/";
    private Path dataPath;
    private Path working;

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

    public String getQaReplayUrl() {
        return qaReplayUrl;
    }

    public void setQaReplayUrl(String qaReplayUrl) {
        this.qaReplayUrl = qaReplayUrl;
    }

    public String getWorkingAreaUrl() {
        return workingAreaUrl;
    }

    public void setWorkingAreaUrl(String workingAreaUrl) {
        this.workingAreaUrl = workingAreaUrl;
    }

    public Path getWorkingDir(Instance instance) {
        return getWorking().resolve(String.valueOf(instance.getPi())).resolve(instance.getDateString());
    }

    public Path getWorking() {
        if (working == null) return getDataPath().resolve("working");
        return working;
    }

    public void setWorking(Path working) {
        this.working = working;
    }

    public Path getDataPath() {
        return dataPath;
    }

    public void setDataPath(Path dataPath) {
        this.dataPath = dataPath;
    }
}
