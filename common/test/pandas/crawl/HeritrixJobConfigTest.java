package pandas.crawl;

import org.junit.Test;
import pandas.crawlconfig.CrawlConfig;
import pandas.crawlconfig.HeritrixJobConfig;
import pandas.crawlconfig.Scope;
import pandas.crawlconfig.Seed;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class HeritrixJobConfigTest {
    @Test
    public void beansXml() {
        CrawlConfig crawlConfig = new CrawlConfig("test", List.of(new Seed("http://example.com/"),
                new Seed("http://example.org/subdir/")));
        String xml = new HeritrixJobConfig(crawlConfig).toXml();
        //System.out.println(xml);
    }

    @Test
    public void urlToSurt() {
        assertEquals("http://(com,example,www,)/dir/index.html", HeritrixJobConfig.urlToSurt("http://www.example.com/dir/index.html", Scope.PAGE));
        assertEquals("http://(com,example,www,)/dir/", HeritrixJobConfig.urlToSurt("http://www.example.com/dir/index.html?q#f", Scope.DIRECTORY));
        assertEquals("http://(com,example,www,:81)/", HeritrixJobConfig.urlToSurt("http://www.example.com:81/dir/index.html?q#f", Scope.HOST));
        assertEquals("http://(com,example,", HeritrixJobConfig.urlToSurt("http://www.example.com:81/dir/index.html?q#f", Scope.DOMAIN));
        assertEquals("http://(com,example,www,)/", HeritrixJobConfig.urlToSurt("http://www.example.com", Scope.PAGE));
        assertEquals("http://(com,example,", HeritrixJobConfig.urlToSurt("http://www.example.com", Scope.AUTO));
        assertEquals("http://(com,example,www,)/", HeritrixJobConfig.urlToSurt("http://www.example.com/foo.html", Scope.AUTO));
    }
}