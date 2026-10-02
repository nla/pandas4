package pandas.gather;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 * Quality indicator for a gather - value must be between zero and one.
 */
@Entity
public class GatherIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GATHER_INDICATOR_SEQ")
    @SequenceGenerator(name = "GATHER_INDICATOR_SEQ", sequenceName = "GATHER_INDICATOR_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "INSTANCE_ID", nullable = false)
    private InstanceGather gather;

    @NotNull
    private GatherIndicator.IndicatorType indicator;

    @NotNull
    @Range(min = 0, max = 1)
    @Column(name = "INDICATOR_VALUE")
    private Float value;

    public GatherIndicator() {
    }

    public GatherIndicator(IndicatorType indicator, float value) {
        this.indicator = indicator;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public InstanceGather getGather() {
        return gather;
    }

    public Float getValue() {
        return value;
    }

    public IndicatorType getIndicator() {
        return indicator;
    }

    public void setGather(InstanceGather gather) {
        this.gather = gather;
    }

    public enum IndicatorType {

        // unknown (i.e. no longer in use)
        UNMAPPED("Unmapped (Unused)"),

        // within (intra) crawl indicators
        HTTP_2XX("HTTP 2XXs (proportion)"),
        HTTP_403("HTTP 403s (proportion)"),
        HTTP_5XX("HTTP 5XXs (proportion)"),
        HTTP_5XX_403("HTTP 5XXs and 403s (proportion)"),
        HTTP_LAST_BAD("Final HTTP response was bad"),

        // Against live, archived, and combo
        LIVE_THUMB_HASH("Live thumbnail difference"),
        ARCHIVED_THUMB_HASH("Archive thumbnail difference"),
        THUMB_HASH("Thumbnail difference"),

        //  increase / decrease
        ARCHIVED_FILE_SIZE_CHANGE("Archived file size change"),
        ARCHIVED_FILE_SIZE_DECREASE("Archived file size decrease"),
        ARCHIVED_HTTP_GOOD_DECREASE("Archived HTTP good decrease"),

        // size too small; useful for number of files too small
        FILE_SIZE_10M("File size 10M"),

        // Combined indicators
        GATHER_VIBE("Overall (gather)"),
        ARCHIVE_VIBE("Overall (archived)"),

        // Bot-blocking response detected in the WARC
        BOT_BLOCKED("Bot blocked"),
        BOT_BLOCKED_AKAMAI("Blocked by Akamai"),
        BOT_BLOCKED_ANUBIS("Blocked by Anubis"),
        BOT_BLOCKED_CLOUDFLARE("Blocked by Cloudflare"),
        BOT_BLOCKED_DATADOME("Blocked by DataDome"),
        BOT_BLOCKED_INCAPSULA("Blocked by Incapsula");

        public final String label;

        IndicatorType(String label) {
            this.label = label;
        }

        public BotBlocker botBlocker() {
            return switch (this) {
                case BOT_BLOCKED_AKAMAI -> BotBlocker.AKAMAI;
                case BOT_BLOCKED_ANUBIS -> BotBlocker.ANUBIS;
                case BOT_BLOCKED_CLOUDFLARE -> BotBlocker.CLOUDFLARE;
                case BOT_BLOCKED_DATADOME -> BotBlocker.DATADOME;
                case BOT_BLOCKED_INCAPSULA -> BotBlocker.INCAPSULA;
                default -> null;
            };
        }

        public static IndicatorType forBotBlocker(BotBlocker blocker) {
            return switch (blocker) {
                case AKAMAI -> BOT_BLOCKED_AKAMAI;
                case ANUBIS -> BOT_BLOCKED_ANUBIS;
                case CLOUDFLARE -> BOT_BLOCKED_CLOUDFLARE;
                case DATADOME -> BOT_BLOCKED_DATADOME;
                case INCAPSULA -> BOT_BLOCKED_INCAPSULA;
            };
        }

    }

    @Converter(autoApply = true)
    public static class JPAConverter implements AttributeConverter<IndicatorType, String> {
        @Override
        public String convertToDatabaseColumn(IndicatorType mType) {
            return mType == null ? null : mType.name();
        }

        @Override
        public IndicatorType convertToEntityAttribute(String s) {
            try {
                return s == null ? null : IndicatorType.valueOf(s);
            } catch (IllegalArgumentException e) {
                return IndicatorType.UNMAPPED;
            }
        }
    }
}
