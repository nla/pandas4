package pandas.social;

import org.netpreserve.jwarc.WarcReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pandas.util.DateFormats;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

@Service
public class AttachmentArchiverTask {
    private static final Logger log = LoggerFactory.getLogger(AttachmentArchiverTask.class);
    private final AttachmentArchiverStateRepository attachmentArchiverStateRepository;
    private final BambooClient bambooClient;
    private final SocialIndexer socialIndexer;
    private final SocialConfig socialConfig;
    private final SocialBambooConfig bambooConfig;

    public AttachmentArchiverTask(AttachmentArchiverStateRepository attachmentArchiverStateRepository, BambooClient bambooClient, SocialIndexer socialIndexer, SocialConfig socialConfig,
                                  SocialBambooConfig bambooConfig) {
        this.attachmentArchiverStateRepository = attachmentArchiverStateRepository;
        this.bambooClient = bambooClient;
        this.socialIndexer = socialIndexer;
        this.socialConfig = socialConfig;
        this.bambooConfig = bambooConfig;
    }

    public void run(boolean dryRun) throws IOException {
        var state = attachmentArchiverStateRepository.findAny();
        if (state == null) state = new AttachmentArchiverState();

        var warcRefs = bambooClient.syncWarcsInCollection(state.getResumptionToken(), 100);

        Instant crawlDate = Instant.now();
        String crawlPid = "nla.arc-social-attachments-" + DateFormats.ARC_DATE.format(crawlDate);
        long crawlId = dryRun ? -1 : bambooClient.createCrawl(bambooConfig.getAttachmentCrawlSeriesId(), crawlPid);
        try (var warcManager = new BambooWarcManager(bambooClient, crawlId, crawlPid, socialIndexer, socialConfig)) {
            AttachmentArchiver archiver = new AttachmentArchiver(warcManager, socialConfig.getCdxServerUrl(), dryRun, socialConfig.getUserAgent());
            log.info("Starting attachment archiving run {}", crawlPid);

            try {
                for (var warcRef : warcRefs) {
                    if (!Objects.equals(state.getWarcId(), warcRef.id())) {
                        state.setWarcId(warcRef.id());
                        state.setWarcOffset(0L);
                    }
                    log.info("Archiving attachments for {} (offset {})", warcRef, state.getWarcOffset());
                    try (var socialReader = new SocialReader(new WarcReader(bambooClient.openWarcWithRangeBuffering(warcRef.id(), state.getWarcOffset())),
                            bambooClient.urlForWarc(warcRef.id()))) {
                        for (var batch = socialReader.nextBatch(); batch != null; batch = socialReader.nextBatch()) {
                            for (var post : batch) {
                                archiver.visit(post, "");
                            }
                            state.setWarcOffset(socialReader.position());
                            if (warcManager.hasReachedLimit()) {
                                warcManager.uploadCurrentFile();
                                state = attachmentArchiverStateRepository.save(state);
                            }
                        }
                        state.setResumptionToken(warcRef.resumptionToken());
                    }
                }
            } finally {
                warcManager.uploadCurrentFile();
                attachmentArchiverStateRepository.save(state);
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
        }, "AttachmentArchiver").start();
    }

    public String status() {
        return "dunno";
    }
}
