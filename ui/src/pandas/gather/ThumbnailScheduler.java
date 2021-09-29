package pandas.gather;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pandas.collection.ThumbnailProcessor;

@Component
@Profile("!test")
public class ThumbnailScheduler {
    private final ThumbnailProcessor thumbnailProcessor;
    private final InstanceThumbnailProcessor instanceThumbnailProcessor;

    public ThumbnailScheduler(ThumbnailProcessor thumbnailProcessor, InstanceThumbnailProcessor instanceThumbnailProcessor) {
        this.thumbnailProcessor = thumbnailProcessor;
        this.instanceThumbnailProcessor = instanceThumbnailProcessor;
    }

    @Scheduled(fixedDelayString = "${pandas.thumbnailProcessor.delay:86400000}", initialDelayString = "${pandas.thumbnailProcessor.initialDelay:0}")
    public void thumbnailTask() {
        thumbnailProcessor.run();
    }

    @Scheduled(fixedDelayString = "${pandas.thumbnailProcessor.delay:86400000}", initialDelayString = "${pandas.thumbnailProcessor.initialDelay:0}")
    public void instanceThumbnailTask() {
        instanceThumbnailProcessor.run();
    }
}
