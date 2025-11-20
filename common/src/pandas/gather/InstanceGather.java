package pandas.gather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.io.FileUtils;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

@Entity
@Table(name = "INS_GATHER")
@DynamicUpdate
public class InstanceGather {
    @Id
    @Column(name = "INSTANCE_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name="INSTANCE_ID")
    @MapsId
    @JsonIgnore
    private Instance instance;

    @OneToMany(mappedBy = "gather", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GatherIndicator> indicators = new ArrayList<>();

    @Column(name = "GATHER_FILES")
    private Long files;

    @Column(name = "GATHER_FINISH")
    private Instant finish;

    @Column(name = "GATHER_RATE")
    private Long rate;

    @Column(name = "GATHER_SIZE")
    private Long size;

    @Column(name = "GATHER_START")
    private Instant start;

    @Column(name = "GATHER_TIME")
    private Long time;

    @Column(name = "EXIT_STATUS")
    private Integer exitStatus;

    public Long getFiles() {
        return this.files;
    }

    public String getFilesHuman() {
        if (files == null) return null;
        return String.format("%,d", files);
    }

    public void setFiles(Long gatherFiles) {
        this.files = gatherFiles;
    }

    public Instant getFinish() {
        return this.finish;
    }

    public void setFinish(Instant gatherFinish) {
        this.finish = gatherFinish;
    }

    public Long getRate() {
        return this.rate;
    }

    public void setRate(Long gatherRate) {
        this.rate = gatherRate;
    }

    public Long getSize() {
        return this.size;
    }

    public String getSizeHuman() {
        if (size == null) return null;
        return FileUtils.byteCountToDisplaySize(size);
    }

    public void setSize(Long gatherSize) {
        this.size = gatherSize;
    }

    public boolean hasSizeWarning() {
        return size != null && size < 1000000;
    }

    public Instant getStart() {
        return this.start;
    }

    public void setStart(Instant gatherStart) {
        this.start = gatherStart;
    }

    public Long getTime() {
        return this.time;
    }

    public String getTimeHuman() {
        if (getStart() == null || getFinish() == null) return null;

        Duration duration = Duration.between(getStart(), getFinish());
        ChronoUnit precision = duration.compareTo(Duration.ofHours(1)) >= 0 ? MINUTES : SECONDS;
        return duration.truncatedTo(precision).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public void setTime(Long gatherTime) {
        this.time = gatherTime;
    }

    public Integer getExitStatus() {
        return this.exitStatus;
    }

    public String getExitStatusHuman() {
        if (exitStatus == null) return null;
        StringBuilder builder = new StringBuilder();
        builder.append(exitStatus);
        if (GatherMethod.BROWSERTRIX.equals(instance.getGatherMethodName())) {
            var exit = BrowsertrixExit.forCode(exitStatus);
            if (exit != null) {
                builder.append(" (").append(exit).append(")");
            }
        }
        return builder.toString();
    }

    public void setExitStatus(Integer exitStatus) {
        this.exitStatus = exitStatus;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getStats() {
        return formatStats(getFiles(), getSize());
    }

    public static String formatStats(Long files, Long size) {
        var builder = new StringBuilder();
        if (files != null) {
            builder.append(String.format("%,d", files));
            builder.append(" files");
        }
        if (size != null) {
            if (builder.length() != 0) {
                builder.append(' ');
            }
            builder.append(FileUtils.byteCountToDisplaySize(size));
        }
        return builder.toString();
    }

    public void setIndicators(List<GatherIndicator> indicators) {
        this.indicators.clear();
        this.indicators.addAll(indicators);
        for (var metric : indicators) {
            metric.setGather(this);
        }
    }

    public GatherIndicator getIndicatorFor(GatherIndicator.IndicatorType type) {
        if (this.getIndicators() == null || this.getIndicators().isEmpty()) {
            return null;
        }
        return this.getIndicators().stream()
                .filter(ind -> ind.getIndicator() == type)
                .findFirst().orElse(null);
    }

    public List<GatherIndicator> getIndicators() {
        return indicators == null ? null
                : indicators.stream()
                         .sorted(Comparator.comparing(GatherIndicator::getIndicator))
                         .collect(Collectors.toUnmodifiableList());
    }

    // "(✭x✦y)" where x is the gather overall score scaled 0-5; y is the same for archive overall score.
    public String getSummaryText() {
        var gather = getIndicatorFor(GatherIndicator.IndicatorType.GATHER_VIBE);
        var archive = getIndicatorFor(GatherIndicator.IndicatorType.ARCHIVE_VIBE);

        String gatherText = gather == null ? "" : String.format("✭%d", Math.round(gather.getValue()*5));
        String archiveText = archive == null ? "" : String.format("✦%d", Math.round(archive.getValue()*5));

        return  String.format("%s%s", gatherText, archiveText);
    }
}
