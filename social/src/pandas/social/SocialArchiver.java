package pandas.social;

import jakarta.annotation.PreDestroy;
import org.netpreserve.jwarc.WarcCompression;
import org.netpreserve.jwarc.WarcWriter;
import org.netpreserve.jwarc.Warcinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pandas.collection.SocialTarget;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardOpenOption.*;

@Service
public class SocialArchiver {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BambooClient bambooClient;
    private final SocialService socialService;
    private final SocialTargetRepository socialTargetRepository;
    private final SocialIndexer socialIndexer;
    private final AdaptiveSearcher adaptiveSearcher;
    private Thread thread;
    private AtomicBoolean stopSignal = new AtomicBoolean();
    private AtomicReference<SocialTarget> currentTarget = new AtomicReference<>();

    public SocialArchiver(SocialConfig socialConfig, BambooClient bambooClient, SocialService socialService,
                          SocialTargetRepository socialTargetRepository,
                          @Autowired(required = false) SocialIndexer socialIndexer) {
        this.adaptiveSearcher = new AdaptiveSearcher(socialConfig.getUserAgent(), stopSignal);
        this.bambooClient = bambooClient;
        this.socialService = socialService;
        this.socialTargetRepository = socialTargetRepository;
        this.socialIndexer = socialIndexer;
    }

    public void run() throws IOException {
        socialService.syncTitlesToSocialTargets();
        var targets = socialTargetRepository.findAll();

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
            long crawlId = bambooClient.createCrawl(crawlPid);

            // Do the actual archiving

            for (var target : targets) {
                log.info("Archiving {}", target);
                currentTarget.set(target);
                try {
                    adaptiveSearcher.search(target.getQuery(), warcWriter, target);
                } catch (Exception e) {
                    log.error("Error archiving {}", target, e);
                }
                if (stopSignal.get()) {
                    log.info("Social archiver stopped, cleaning up.");
                    break;
                }
            }
            currentTarget.set(null);

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

    private void tryRun() {
        try {
            run();
        } catch (Exception e) {
            log.error("Archiving error", e);
        }
    }

    public synchronized void start() {
        // start run() in a thread only if not already running
        if (thread == null || !thread.isAlive()) {
            stopSignal.set(false);
            thread = new Thread(this::tryRun);
            thread.start();
        }
    }

    public synchronized void stop() {
        stopSignal.set(true);
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PreDestroy
    public void close() {
        stop();
    }

    public String status() {
        if (thread == null || !thread.isAlive()) return "Not running";
        var target = currentTarget.get();
        String prefix = stopSignal.get() ? "[Stopping] " : "";
        if (target == null) return prefix + "Ingesting";
        return prefix + "Archiving " + target + " (range " + target.getCurrentRangePosition() + " - " + target.getCurrentRangeEnd() + ")";
    }
}
