package pandas.gatherer.core;

import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.gather.GatherIndicator;
import pandas.gather.Instance;
import pandas.gather.InstanceThumbnail;
import pandas.gather.State;
import pandas.search.FileSearcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@Service
public class GathererIndicatorService {

    private final Config config;
    private final WorkingArea workingArea;

    private static final Logger log = LoggerFactory.getLogger(GathererIndicatorService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public GathererIndicatorService(Config config, WorkingArea workingArea) {
        this.config = config;
        this.workingArea = workingArea;
    }

    @Transactional
    public List<GatherIndicator> getIndicators(Instance instance) throws IOException {

        List<GatherIndicator> result = new ArrayList<>();

        Instance current = entityManager.merge(instance);  // lazy load thumbs -> transaction -> attach to PC
        Instance lastArchived = getMostRecentArchived(current);

        // 'intra' gather indicators - filesearcher
        try {
            Path indexDir = workingArea.getInstanceDir(current.getTitle().getPi(), current.getDateString()).resolve("fileindex");
            FileSearcher fileSearcher = new FileSearcher(indexDir);

            addIfComputable(result,
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.HTTP_403, scoreHttpStatuses(fileSearcher, 403, 403)),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.HTTP_5XX, scoreHttpStatuses(fileSearcher, 500, 599)),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.HTTP_2XX, scoreHttpStatuses(fileSearcher, 200, 299)),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.HTTP_LAST_BAD, scoreLastStatusBad(fileSearcher)),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.FILE_SIZE_10M, scoreScaleTo(10_000_000, current.getGather().getSize()))
            );
        }
        catch (IOException e) {
            log.warn("FileSearcher error (continuing)", e);
            return new ArrayList<>();
        }

        // last archived
        if (lastArchived != null) {
            addIfComputable(result,
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.ARCHIVED_THUMB_HASH, scoreReplayArchivedThumbnailDifference(current, lastArchived)),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.ARCHIVED_FILE_SIZE_DECREASE, scoreDecreaseArchived(current.getGather().getSize(), lastArchived.getGather().getSize())),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.ARCHIVED_FILE_SIZE_CHANGE, scoreChange(current.getGather().getSize(), lastArchived.getGather().getSize())),
                    () -> new GatherIndicator(GatherIndicator.IndicatorType.ARCHIVED_HTTP_GOOD_DECREASE, scoreDecreased(GatherIndicator.IndicatorType.HTTP_2XX, current, lastArchived))
            );
        }

        // live indicator
        addIfComputable(result,
                () -> new GatherIndicator(GatherIndicator.IndicatorType.LIVE_THUMB_HASH, scoreLiveReplayThumbnailDifference(current))
        );

        // finally - composite indicators
        var unModifiableResults = Collections.unmodifiableList(result);
        addIfComputable(result,
                () -> new GatherIndicator(GatherIndicator.IndicatorType.GATHER_VIBE, scoreIntraVibe(unModifiableResults)),
                () -> new GatherIndicator(GatherIndicator.IndicatorType.ARCHIVE_VIBE, scoreArchivedVibe(unModifiableResults)),
                () -> new GatherIndicator(GatherIndicator.IndicatorType.THUMB_HASH, scoreCombinedThumbnailDifference(unModifiableResults)),
                () -> new GatherIndicator(GatherIndicator.IndicatorType.HTTP_5XX_403, scoreCombined5xx403(unModifiableResults))
        );

        return result;
    }

    private Instance getMostRecentArchived(Instance instance) {
        var r = instance.getTitle().getInstances().subList(0, instance.getTitle().getInstances().size() - 1);
        Collections.reverse(r);
        return r.stream()
                .filter(s -> s.getState() == State.ARCHIVED)
                .findFirst().orElse(null);
    }

    // Proportion of statuses in range (inclusive)
    private float scoreHttpStatuses(FileSearcher fileSearcher, int lower, int upper) throws IOException {

        var statusCounts = fileSearcher.countStatuses();
        int total = 0, n = 0;

        for (String e : statusCounts.keySet()) {

            int code = Integer.parseInt(e);
            int count = statusCounts.get(e);

            if (code >= lower && code <= upper) {
                n += count;
            }
            total += count;
        }
        return total == 0 ? 0 : (float) n/total;
    }

    // 1.0 if last response was a bad status to end on, 0 otherwise.  (404 is 'ok'.)
    private float scoreLastStatusBad(FileSearcher fileSearcher) throws IOException {
        if (fileSearcher.getLast().isPresent()) {
            int status = fileSearcher.getLast().get().status();
            if (status == 401 || status == 403 || status >= 500) {
                return 1;
            }
        }
        return 0;
    }

    // decrease factor: 0.9 means size decreased by 90%; 0 means no decrease.
    private float scoreDecreaseArchived(Float currentValue, Float archivedValue)  {
        if (archivedValue != null && currentValue != null) {
            return 1f - Math.min(currentValue/archivedValue, 1f);
        }
        throw new IllegalArgumentException("can't calculate decrease - value is null");
    }

    private float scoreDecreaseArchived(Long currentValue, Long archivedValue)  {
        if (archivedValue != null && currentValue != null) {
            return 1f - Math.min((float)currentValue/archivedValue, 1f);
        }
        throw new IllegalArgumentException("can't calculate decrease - value is null");
    }

    // decrease factor: 0.9 means decreased by 90%; 0 means no decrease.
    private float scoreDecreased(GatherIndicator.IndicatorType indicatorType, Instance instance, Instance archived) {
        GatherIndicator archivedIndicator = archived.getGather().getIndicatorFor(indicatorType);
        GatherIndicator instanceIndicator = instance.getGather().getIndicatorFor(indicatorType);
        return scoreDecreaseArchived(archivedIndicator.getValue(), instanceIndicator.getValue());
    }

    // change factor: 0.1 means instance within 10% of archived, max 100%.
    private float scoreChange(Long currentValue, Long archivedValue) {
        if (archivedValue != null && currentValue != null) {
            return Math.min(Math.abs((float)(currentValue-archivedValue)/archivedValue), 1);
        }
        throw new IllegalArgumentException("can't calculate change - value is null");
    }

    private float scoreScaleTo(long maximum, Long value) {
        if (value != null) {
            return Math.min(Math.abs((float)(value)/maximum), 1);
        }
        throw new IllegalArgumentException("can't calculate scale - value is null");
    }

    // Similarity score - 0 (similar) to 1.0 (dissimilar)
    private float scoreReplayArchivedThumbnailDifference(Instance instance, Instance archivedInstance) throws IOException {
        return scoreThumbnailDifference(instance.getThumbnail(), archivedInstance.getThumbnail());
    }

    // Similarity score - 0 (similar) to 1.0 (dissimilar)
    private float scoreLiveReplayThumbnailDifference(Instance instance) throws IOException {
        return scoreThumbnailDifference(instance.getLiveThumbnail(), instance.getThumbnail());
    }

    private float scoreThumbnailDifference(InstanceThumbnail thumbnail1, InstanceThumbnail thumbnail2)
            throws IOException {

        if (thumbnail1 == null || thumbnail2 == null) {
            throw new IllegalArgumentException("image is null");
        }

        BufferedImage liveImage   = ImageIO.read(new ByteArrayInputStream(thumbnail1.getData()));
        BufferedImage replayImage = ImageIO.read(new ByteArrayInputStream(thumbnail2.getData()));

        HashingAlgorithm hashingAlgorithm = new PerceptiveHash(40);
        var hash = hashingAlgorithm.hash(liveImage);

        return (float) hash.normalizedHammingDistance(hashingAlgorithm.hash(replayImage));
    }

    public float scoreIntraVibe(List<GatherIndicator> indicators) {

        long weight = 0, cumulativeWeight = 0;
        float score = 0, cumulativeScore = 0;  // 0 is "bad", 1 is "good"

        for (GatherIndicator ind : indicators) {
            switch(ind.getIndicator()) {
                case HTTP_2XX  -> {
                    weight = 2; score = ind.getValue();
                }
                case FILE_SIZE_10M -> { // 1MB
                    weight = 4; score = ind.getValue() < 0.1 ? 0 : 1;
                }
                case HTTP_403, HTTP_5XX ->  {
                    weight = 1; score = (1 - ind.getValue());
                }
                case HTTP_LAST_BAD -> {
                    weight = 4; score = (1 - ind.getValue());
                }
                case LIVE_THUMB_HASH -> {
                    weight = 1; score = (float) (1 -  ind.getValue() == 0 ? 0 : Math.cbrt(ind.getValue()));
                }
                default -> {
                    weight = 0; score = 0;
                }
            }
            cumulativeScore += weight * score;
            cumulativeWeight += weight;
        }

        if (cumulativeWeight == 0) {
            throw new IllegalArgumentException("Can't calculate vibe - no indicators present");
        }

        return cumulativeScore/cumulativeWeight;
    }

    // against archived version
    public float scoreArchivedVibe(List<GatherIndicator> indicators) {

        long weight = 0, cumulativeWeight = 0;
        float score = 0, cumulativeScore = 0;  // 0 is "bad", 1 is "good"

        for (GatherIndicator ind : indicators) {
            switch(ind.getIndicator()) {
                case ARCHIVED_THUMB_HASH -> {
                    weight = 1; score = (float) (1 - Math.cbrt(ind.getValue()));
                }
                case ARCHIVED_FILE_SIZE_DECREASE -> {
                    weight = 1; score = (1 - ind.getValue());
                }
                case ARCHIVED_HTTP_GOOD_DECREASE -> {
                    weight = 2; score = (1 - ind.getValue());
                }
                default -> {
                    weight = 0; score = 0;
                }
            }
            cumulativeScore += weight * score;
            cumulativeWeight += weight;
        }

        if (cumulativeWeight == 0) {
            throw new IllegalArgumentException("Can't calculate archived vibe - no indicators present");
        }

        return cumulativeScore/cumulativeWeight;
    }

    public GatherIndicator getIndicatorFor(List<GatherIndicator> indicators, GatherIndicator.IndicatorType indicatorType) {
        if (indicators == null || indicators.isEmpty()) {
            return null;
        }
        return indicators.stream()
                .filter(ind -> ind.getIndicator() == indicatorType)
                .findFirst().orElse(null);
    }

    // proportion that are 403 or 5xx
    public float scoreCombined5xx403(List<GatherIndicator> indicators) {
        var ind403 = getIndicatorFor(indicators, GatherIndicator.IndicatorType.HTTP_403);
        var ind5xx = getIndicatorFor(indicators, GatherIndicator.IndicatorType.HTTP_5XX);

        return (ind403 == null ? 0 : ind403.getValue()) + (ind5xx == null ? 0 : ind5xx.getValue());
    }

    public float scoreCombinedThumbnailDifference(List<GatherIndicator> indicators) {

        long weight = 0, cumulativeWeight = 0;
        float score = 0, cumulativeScore = 0;  // 0 is "bad", 1 is "good"

        for (GatherIndicator ind : indicators) {
            switch (ind.getIndicator()) {
                case ARCHIVED_THUMB_HASH, LIVE_THUMB_HASH -> {
                    weight = 1;
                    score = ind.getValue();
                }
                default -> {
                    weight = 0;
                    score = 0;
                }
            }
            cumulativeScore += weight * score;
            cumulativeWeight += weight;
        }

        if (cumulativeWeight == 0) {
            throw new IllegalArgumentException("Can't calculate combined thumbnail diff - no indicators present");
        }

        return cumulativeScore / cumulativeWeight;
    }

    private void addIfComputable(List<GatherIndicator> indicators, Callable<GatherIndicator>... inds)  {
        for (var c: inds) {
            try {
                indicators.add(c.call());
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error calculating indicator (continuing)", e);
                } else {
                    log.warn("Error calculating indicator (continuing): {}", e.getMessage());
                }
            }
        }
    }

}
