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
        Title title = new Title();
        title.setPi(1234L);
        title.setGather(new TitleGather());
        title.getGather().setGatherUrl("http://example.com/");
        var date = LocalDateTime.parse("2021-05-03T16:17:41").atZone(ZoneId.systemDefault()).toInstant();
        var instance = new Instance(title, date, null, "HTTrack");
        instance.setTepUrl("/pan/1234/12345678-0000/example.com/index.html");
        assertEquals("https://pandas.nla.gov.au/view/1234/20210503-1617/example.com/index.html", urls.qa(instance));
        assertEquals("https://pandas.nla.gov.au/view/1234/20210503-1617/hts-cache/new.txt", urls.crawlLog(instance));
    }
}