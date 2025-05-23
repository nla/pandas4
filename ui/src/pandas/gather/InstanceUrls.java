package pandas.gather;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pandas.core.Config;

import java.net.URI;

@Service
public class InstanceUrls {
    private static final Logger log = LoggerFactory.getLogger(InstanceUrls.class);
    private final Config config;

    public InstanceUrls(Config config) {
        this.config = config;
    }

    @NotNull
    private String workingAreaBase(Instance instance) {
        return config.getWorkingAreaUrl() + instance.getPi() + "/" + instance.getDateString();
    }

    @NotNull
    private String jobDir(Instance instance) {
        return workingAreaBase(instance) + "/nla.arc-" + instance.getPi() + "-" + instance.getDateString() + "/latest";
    }

    public String crawlLog(Instance instance) {
        if (instance.isBrowsertrixMethod()) {
            return workingAreaBase(instance) + "/stdio.log";
        } else if (instance.isFlatFiles()) {
            return workingAreaBase(instance) + "/hts-cache/new.txt";
        } else {
            return jobDir(instance) + "/logs/crawl.log";
        }
    }

    public String qa(Instance instance) {
        if (instance.isFlatFiles()) {
            return workingAreaBase(instance) + instance.getTepUrl().replaceFirst("/pan/[0-9]+/[0-9-]+/", "/");
        }
        return config.getQaReplayUrl() + instance.getPi() + "-" + instance.getDateString() + "/mp_/" +
                instance.getTepUrlAbsolute();
    }

    public String qaExperimental(Instance instance) {
        if (instance.isFlatFiles()) {
            return workingAreaBase(instance) + instance.getTepUrl().replaceFirst("/pan/[0-9]+/[0-9-]+/", "/");
        }
        return config.getExperimentalQaReplayUrl() + "wabac/" + instance.getPi() + "-" + instance.getDateString() + "/2/" +
               instance.getTepUrlAbsolute();
    }

    public String qa(Instance instance, String relative) {
        if (relative == null) return qa(instance);
        String url = URI.create(instance.getTepUrlAbsolute()).resolve(relative).toASCIIString();
        if (instance.isFlatFiles()) {
            return workingAreaBase(instance) + url.replaceFirst("^https?://", "/");
        }
        return qaReplayBase(instance) + "/mp_/" + url;
    }

    @NotNull
    public String qaReplayBase(Instance instance) {
        return config.getQaReplayUrl() + instance.getPi() + "-" + instance.getDateString();
    }

    public String reports(Instance instance) {
        return jobDir(instance) + "/reports/";
    }
}