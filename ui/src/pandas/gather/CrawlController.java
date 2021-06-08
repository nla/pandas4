package pandas.gather;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.Title;
import pandas.collection.TitleRepository;
import pandas.crawlconfig.CrawlConfig;
import pandas.crawlconfig.HeritrixJobConfig;
import pandas.crawlconfig.Seed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class CrawlController {
    private static final Pattern TWITTER_URL = Pattern.compile("https://twitter.com/([^/?#]+)");

    private final TitleRepository titleRepository;

    public CrawlController(TitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    @GetMapping(value = "/crawls/config", produces = "application/json")
    @ResponseBody
    public CrawlConfig config() {
        List<Seed> seeds = new ArrayList<>();
        for (Title title: titleRepository.findBulkTitles(Instant.now())) {
            for (String seedUrl: title.getAllSeeds()) {
                seeds.add(new Seed(seedUrl));
//                var m = TWITTER_URL.matcher(seedUrl);
//                if (m.matches()) {
//                    String username = m.group(1);
//                    surts.add("https://twitter.com/" + username + "/status/");
//
//                }
            }
        }

        return new CrawlConfig("test", seeds);
    }

    @GetMapping(value = "/crawls/heritrix", produces = "application/xml")
    @ResponseBody
    public String heritrix() {
        return new HeritrixJobConfig(config()).toXml();
    }
}
