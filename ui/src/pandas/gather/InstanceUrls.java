package pandas.gather;

import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class InstanceUrls {
    public String crawlLog(Instance instance) {
        return "https://pandas.nla.gov.au/view/" + instance.getPi() + "/" + instance.getDateString() + "/nla.arc-" +
                instance.getPi() + "-" + instance.getDateString() + "/latest/logs/crawl.log";
    }

    public String qa(Instance instance) {
        return "https://pwb.archive.org.au/" + instance.getPi() + "-" + instance.getDateString() + "/mp_/" +
                instance.getGatheredUrl();
    }

    public String qa(Instance instance, String relative) {
        return "https://pwb.archive.org.au/" + instance.getPi() + "-" + instance.getDateString() + "/mp_/" +
                URI.create(instance.getGatheredUrl()).resolve(relative).toASCIIString();
    }
}