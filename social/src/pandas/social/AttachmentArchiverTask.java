package pandas.social;

import org.netpreserve.jwarc.WarcReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pandas.util.DateFormats;

import java.io.IOException;
import java.time.Instant;

@Service
public class AttachmentArchiverTask {
    private static final Logger log = LoggerFactory.getLogger(AttachmentArchiverTask.class);
    private final BambooClient bambooClient;
    private final SocialIndexer socialIndexer;
    private final SocialConfig socialConfig;
    private final SocialBambooConfig bambooConfig;

    public AttachmentArchiverTask(BambooClient bambooClient, SocialIndexer socialIndexer, SocialConfig socialConfig,
                                  SocialBambooConfig bambooConfig) {
        this.bambooClient = bambooClient;
        this.socialIndexer = socialIndexer;
        this.socialConfig = socialConfig;
        this.bambooConfig = bambooConfig;
    }

    public void run(boolean dryRun) throws IOException {
        String resumptionToken = null;
        var warcRefs = bambooClient.syncWarcsInCollection(resumptionToken, 100);

        Instant crawlDate = Instant.now();
        String crawlPid = "nla.arc-social-attachments-" + DateFormats.ARC_DATE.format(crawlDate);
        long crawlId = dryRun ? -1 : bambooClient.createCrawl(bambooConfig.getAttachmentCrawlSeriesId(), crawlPid);
        try (var warcManager = new BambooWarcManager(bambooClient, crawlId, crawlPid, socialIndexer, socialConfig)) {
            AttachmentArchiver archiver = new AttachmentArchiver(warcManager, socialConfig.getCdxServerUrl(), dryRun, socialConfig.getUserAgent());
            log.info("Starting attachment archiving run {}", crawlPid);

            try {
                for (var warcRef : warcRefs) {
                    log.info("Archiving attachments for {}", warcRef);
                    try (var socialReader = new SocialReader(new WarcReader(bambooClient.openWarc(warcRef.id())), bambooClient.urlForWarc(warcRef.id()))) {
                        archiver.processWarc(socialReader);
                        if (warcManager.hasReachedLimit()) {
                            warcManager.uploadCurrentFile();
                        }
                    }
                }
            } finally {
                warcManager.uploadCurrentFile();
                log.info("Finished archiving attachments");
            }
        }
    }

    public void start(boolean dryRun) {
        new Thread(() -> {
            try {
                this.run(dryRun);
            } catch (IOException e) {
                log.error("Attachment archiver exception", e);
            }
        }).start();
    }

    public String status() {
        return "dunno";
    }
}
