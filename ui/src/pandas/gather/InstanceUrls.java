package pandas.gather;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class InstanceUrls {
    @NotNull
    private String workingAreaBase(Instance instance) {
        return "https://pandas.nla.gov.au/view/" + instance.getPi() + "/" + instance.getDateString();
    }

    public String crawlLog(Instance instance) {
        return workingAreaBase(instance) + "/nla.arc-" + instance.getPi() + "-" + instance.getDateString() + "/latest/logs/crawl.log";
    }

    public String qa(Instance instance) {
        if (instance.isFlatFiles()) {
            return workingAreaBase(instance) + instance.getTepUrl().replaceFirst("/pan/[0-9+]/[0-9-+]/", "/");
        }
        return "https://pwb.archive.org.au/" + instance.getPi() + "-" + instance.getDateString() + "/mp_/" +
                instance.getGatheredUrl();
    }

    public String qa(Instance instance, String relative) {
        String url = URI.create(instance.getGatheredUrl()).resolve(relative).toASCIIString();
        if (instance.isFlatFiles()) {
            return workingAreaBase(instance) + url.replaceFirst("^https?://", "/");
        }
        return "https://pwb.archive.org.au/" + instance.getPi() + "-" + instance.getDateString() + "/mp_/" + url;
    }
}