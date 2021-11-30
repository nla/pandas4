package pandas.gather;

import org.junit.Test;
import pandas.collection.Title;
import pandas.core.Config;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

public class InstanceUrlsTest {
    @Test
    public void httrackInstanceUrls() {
        var urls = new InstanceUrls(new Config());
        var instance = new Instance();
        Title title = new Title();
        title.setPi(1234L);
        instance.setTitle(title);
        instance.setGatherMethodName("HTTrack");
        instance.setGatheredUrl("http://example.com/");
        instance.setTepUrl("/pan/1234/12345678-0000/example.com/index.html");
        instance.setDate(LocalDateTime.parse("2021-05-03T16:17:41").atZone(ZoneId.systemDefault()).toInstant());
        assertEquals("https://pandas.nla.gov.au/view/1234/20210503-1617/example.com/index.html", urls.qa(instance));
        assertEquals("https://pandas.nla.gov.au/view/1234/20210503-1617/hts-cache/new.txt", urls.crawlLog(instance));
    }
}