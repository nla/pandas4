package pandas.social;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pandas.collection.SocialTarget;
import pandas.collection.SocialTargetRepository;
import pandas.social.mastodon.MastodonVisitor;
import pandas.social.twitter.AdaptiveSearcher;
import pandas.social.twitter.GraphqlVisitor;
import pandas.social.twitter.SessionManager;
import pandas.util.DateFormats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SocialArchiver {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BambooClient bambooClient;
    private final SocialService socialService;
    private final SocialTargetRepository socialTargetRepository;
    private final SocialIndexer socialIndexer;
    private final AdaptiveSearcher adaptiveSearcher;
    private final SocialConfig socialConfig;
    private final SessionManager sessionManager;
    private final SocialBambooConfig bambooConfig;
    private Thread thread;
    private AtomicBoolean stopSignal = new AtomicBoolean();
    private AtomicReference<SocialTarget> currentTarget = new AtomicReference<>();
    private volatile String status = "Idle";

    public SocialArchiver(SocialConfig socialConfig, SocialBambooConfig bambooConfig,
                          BambooClient bambooClient, SocialService socialService,
                          SocialTargetRepository socialTargetRepository,
                          @Autowired(required = false) SocialIndexer socialIndexer) {
        this.sessionManager = new SessionManager(socialConfig.getUserAgent());
        this.adaptiveSearcher = new AdaptiveSearcher(sessionManager, stopSignal);
        this.bambooConfig = bambooConfig;
        this.bambooClient = bambooClient;
        this.socialService = socialService;
        this.socialTargetRepository = socialTargetRepository;
        this.socialIndexer = socialIndexer;
        this.socialConfig = socialConfig;
    }

    public void run() throws IOException {
        try {
            status = "Syncing titles to targets";
            socialService.syncTitlesToSocialTargets();

            if (findCandidateTargets().isEmpty()) {
                log.info("No targets to archive, not running crawl");
                return;
            }

            var crawlDate = Instant.now();
            var crawlPid = "nla.arc-social-" + DateFormats.ARC_DATE.format(crawlDate);
            long crawlId = bambooClient.createCrawl(bambooConfig.getCrawlSeriesId(), crawlPid);
            try (var warcManager = new BambooWarcManager(bambooClient, crawlId, crawlPid, socialIndexer, socialConfig)) {
                while (!stopSignal.get()) {
                    var targets = findCandidateTargets();
                    if (targets.isEmpty()) {
                        log.info("Finished all targets");
                        break;
                    }

                    archiveTargets(crawlId, warcManager, targets);

                    if (!stopSignal.get()) {
                        int sleepMillis = 10 * 1000;
                        log.info("Waiting {}ms before starting next file", sleepMillis);
                        try {
                            Thread.sleep(sleepMillis);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } finally {
            status = "Idle";
        }
    }

    private void archiveTargets(long crawlId, BambooWarcManager bambooWarcManager, List<SocialTarget> targets) throws IOException {
        // Fetch records from targets and write to a temporary warc file
        Path tempFile = Files.createTempFile("pandas-social-", ".warc.gz");

        try {
            // Do the actual archiving
            status = "Archiving";
            for (var target : targets) {
                log.info("Archiving {}", target);
                currentTarget.set(target);
                try {
                    if (target.getServer().equals("twitter.com")) {
                        // Disabled for now
                        // new GraphqlVisitor(sessionManager, stopSignal).visitTarget(target, bambooWarcManager.writer());
                    } else {
                        new MastodonVisitor(socialConfig.getUserAgent(), stopSignal).visitTarget(target, bambooWarcManager.writer());
                    }
                } catch (Exception e) {
                    log.error("Error archiving {}, stopping.", target, e);
                    stopSignal.set(true);
                    break;
                }
                if (stopSignal.get()) {
                    log.info("Social archiver stopped, cleaning up.");
                    break;
                }
                if (bambooWarcManager.hasReachedLimit()) {
                    log.info("Reached WARC size or time limit, finishing file.");
                    break;
                }
            }

            currentTarget.set(null);

            // Upload to bamboo if we actually wrote anything
            status = "Uploading " + bambooWarcManager.currentFilename();
            bambooWarcManager.uploadCurrentFile();

            // Now the WARC is safely uploaded we can update the target state
            status = "Updating targets in database";
            socialTargetRepository.saveAll(targets);
        } finally {
            status = "Idle";
        }
    }

    private List<SocialTarget> findCandidateTargets() {
        status = "Finding candidate targets";
        Instant timeCutoff = Instant.now().minusMillis(socialConfig.getArchivingIntervalMillis());
        Pageable pageable = PageRequest.of(0, socialConfig.getMaxTargetsPerWarcFile());
        return socialTargetRepository.findArchivingCandidates(timeCutoff, pageable);
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
            thread = new Thread(this::tryRun, "SocialArchiver");
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
        if (target == null) return status;
        return prefix + "Archiving " + target + " (range " + target.getCurrentRangePosition() + " - " + target.getCurrentRangeEnd() + ")";
    }
}
