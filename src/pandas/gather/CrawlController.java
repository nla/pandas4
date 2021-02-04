package pandas.gather;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.Title;
import pandas.collection.TitleRepository;

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

    @GetMapping(value = "/crawls/config", produces = "text/xml")
    @ResponseBody
    public Object config() {
        List<String> seeds = new ArrayList<>();
        List<String> surts = new ArrayList<>();
        for (Title title: titleRepository.findBulkTitles(Instant.now())) {
            seeds.addAll(title.getAllSeeds());
            for (String seed: seeds) {
                var m = TWITTER_URL.matcher(seed);
                if (m.matches()) {
                    String username = m.group(1);
                    surts.add("https://twitter.com/" + username + "/status/");

                }
            }
        }

        HeritrixConfig config = new HeritrixConfig();
        config.jobName = "test";
        config.description = "test";
        config.seeds = seeds;
        config.surts = surts;
        return config.beansXml();
    }
}
