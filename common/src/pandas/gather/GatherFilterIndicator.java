package pandas.gather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Indicators used for RHS facet style filtering.  Each is defined by a gather (GatherIndicator.IndicatorType), label,
// and predicate (for when to apply).  Reindex on change.
public enum GatherFilterIndicator {

    // within gather
    MANY_2XX_RESPONSES (GatherIndicator.IndicatorType.HTTP_2XX,      "Many HTTP 2XXs", v -> v > 0.5),
    MANY_5XX_RESPONSES (GatherIndicator.IndicatorType.HTTP_5XX,      "Many HTTP 5XXs", v -> v > 0.4),
    MANY_403_RESPONSES (GatherIndicator.IndicatorType.HTTP_403,      "Many HTTP 403s", v -> v > 0.4),
    LAST_RESPONSE_BAD  (GatherIndicator.IndicatorType.HTTP_LAST_BAD, "Bad final HTTP response", v -> v > 0.5),
    MANY_5XX_403_RESPONSES (GatherIndicator.IndicatorType.HTTP_5XX_403, "Many HTTP 5XXs or 403s", v -> v > 0.4),

    // less than 1M
    FILE_SIZE_SMALL  (GatherIndicator.IndicatorType.FILE_SIZE_10M, "Too small", v -> v < 0.1),

    // thumbnails - against live and archived
    CRAWL_VS_LIVE_THUMB_DIFF     (GatherIndicator.IndicatorType.LIVE_THUMB_HASH,     "Live thumbnail differs", v -> v > 0.3),
    CRAWL_VS_ARCHIVED_THUMB_DIFF (GatherIndicator.IndicatorType.ARCHIVED_THUMB_HASH, "Archived thumbnail differs", v -> v > 0.3),
    CRAWL_VS_LIVE_THUMB_SIM      (GatherIndicator.IndicatorType.LIVE_THUMB_HASH,     "Live thumbnail similar", v -> v < 0.3),
    CRAWL_VS_ARCHIVED_THUMB_SIM  (GatherIndicator.IndicatorType.ARCHIVED_THUMB_HASH, "Archived thumbnail similar", v -> v < 0.3),

    // thumbnails - experimental combo
    CRAWL_THUMB_DIFF (GatherIndicator.IndicatorType.THUMB_HASH, "Thumbnail differs", v -> v > 0.3),
    CRAWL_THUMB_SIM  (GatherIndicator.IndicatorType.THUMB_HASH, "Thumbnail similar", v -> v < 0.3),

    // similar to archived
    FILE_SIZE_SIMILAR    (GatherIndicator.IndicatorType.ARCHIVED_FILE_SIZE_CHANGE,   "Similar size", v -> v < 0.2),
    FILE_SIZE_DECREASED  (GatherIndicator.IndicatorType.ARCHIVED_FILE_SIZE_DECREASE, "Too small",  v -> v > 0.3),
    MANY_GOOD_DECREASED  (GatherIndicator.IndicatorType.ARCHIVED_HTTP_GOOD_DECREASE, "Too few HTTP 2XXs", v -> v > 0.3),

    // files / count
    OVERALL_LOW_GATHER    (GatherIndicator.IndicatorType.GATHER_VIBE,  "Low confidence",   v -> v < 0.5),
    OVERALL_HIGH_GATHER   (GatherIndicator.IndicatorType.GATHER_VIBE,  "High confidence",  v -> v > 0.9),
    OVERALL_LOW_ARCHIVED  (GatherIndicator.IndicatorType.ARCHIVE_VIBE, "Low confidence",   v -> v < 0.7),
    OVERALL_HIGH_ARCHIVED (GatherIndicator.IndicatorType.ARCHIVE_VIBE, "High confidence",  v -> v > 0.8),
    OVERALL_V_HIGH_ARCHIVED (GatherIndicator.IndicatorType.ARCHIVE_VIBE, "Very high confidence", v -> v > 0.9),
    PREVIOUSLY_ARCHIVED   (GatherIndicator.IndicatorType.ARCHIVE_VIBE, "Previously archived",    v -> true),
    ;

    public static Set<GatherFilterIndicator> goodIndicators = Set.of(
            MANY_2XX_RESPONSES,
            PREVIOUSLY_ARCHIVED, FILE_SIZE_SIMILAR,
            OVERALL_HIGH_ARCHIVED
    );

    public static Set<GatherFilterIndicator> poorIndicators = Set.of(
            LAST_RESPONSE_BAD, MANY_5XX_403_RESPONSES, FILE_SIZE_SMALL,
            FILE_SIZE_DECREASED, MANY_GOOD_DECREASED, OVERALL_LOW_GATHER, OVERALL_LOW_ARCHIVED
    );

    private final String text;
    private final GatherIndicator.IndicatorType indicatorType;
    private final Predicate<Float> predicate;

    GatherFilterIndicator(GatherIndicator.IndicatorType indicatorType, String text, Predicate<Float> predicate) {
        this.indicatorType = indicatorType;
        this.predicate = predicate;
        this.text = text;
    }

    public static List<GatherFilterIndicator> findAllById(Iterable<Long> ids) {
        var vibes = new ArrayList<GatherFilterIndicator>();
        for (var id : ids) {
            vibes.add(GatherFilterIndicator.values()[(int) (long) id]);
        }
        return vibes;
    }

    public boolean satisfiedBy(Float value) {
        return predicate.test(value);
    }

    public String text() {
        return text;
    }

    public Long id() {
        return (long) ordinal();
    }

    public static Set<GatherFilterIndicator> byIndicatorType(GatherIndicator.IndicatorType indicatorType) {
        return Arrays.stream(GatherFilterIndicator.values())
                .filter(v->v.indicatorType == indicatorType)
                .collect(Collectors.toSet());
    }

}
