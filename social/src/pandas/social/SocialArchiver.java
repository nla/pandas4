package pandas.social;

import org.netpreserve.jwarc.WarcCompression;
import org.netpreserve.jwarc.WarcWriter;
import org.netpreserve.jwarc.Warcinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pandas.collection.SocialTargetRepository;
import pandas.social.twitter.AdaptiveSearcher;
import pandas.util.DateFormats;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;

@Service
public class SocialArchiver {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BambooClient bambooClient;
    private final SocialService socialService;
    private final SocialTargetRepository socialTargetRepository;
    private final SocialConfig config;
    private final SocialIndexer socialIndexer;

    public SocialArchiver(SocialConfig socialConfig, BambooClient bambooClient, SocialService socialService,
                          SocialTargetRepository socialTargetRepository,
                          @Autowired(required = false) SocialIndexer socialIndexer) {
        this.config = socialConfig;
        this.bambooClient = bambooClient;
        this.socialService = socialService;
        this.socialTargetRepository = socialTargetRepository;
        this.socialIndexer = socialIndexer;
    }

    public void run() throws IOException {
        socialService.syncTitlesToSocialTargets();
        var targets = socialTargetRepository.findAll();

        long crawlSeriesId = 1;
        var crawlDate = Instant.now();
        var crawlPid = "nla.arc-social-" + DateFormats.ARC_DATE.format(crawlDate);

        // Fetch records from targets and write to a temporary warc file
        Path tempFile = Files.createTempFile("pandas-social-", ".warc.gz");
        try (FileChannel channel = FileChannel.open(tempFile, DELETE_ON_CLOSE, READ, WRITE)) {
            String filename = crawlPid + ".warc.gz";
            WarcWriter warcWriter = new WarcWriter(channel, WarcCompression.GZIP);
            warcWriter.write(new Warcinfo.Builder()
                    .filename(filename)
                    .fields(Map.of("software", List.of("pandas-social")))
                    .build());

            long start = warcWriter.position();

            // Create the crawl in bamboo, we do this now so if bamboo is down we fail out early
            long crawlId = bambooClient.createCrawl(crawlSeriesId, crawlPid);

            // Do the actual archiving
            AdaptiveSearcher adaptiveSearcher = new AdaptiveSearcher(config.getUserAgent());
            for (var target : targets) {
                log.info("Archiving {}", target);
                try {
                    adaptiveSearcher.search(target.getQuery(), warcWriter, target);
                } catch (Exception e) {
                    log.error("Error archiving {}", target, e);
                }
            }

            // Upload to bamboo if we actually wrote anything
            if (warcWriter.position() > start) {
                channel.position(0);
                long warcId = bambooClient.putWarcIfNotExists(crawlId, filename, channel, channel.size());
                socialIndexer.enqueueWarcId(warcId);
            }

            // Now the WARC is safely uploaded we can update the target state
            socialTargetRepository.saveAll(targets);
        }
    }
}
