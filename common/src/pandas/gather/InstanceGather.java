package pandas.gather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Duration;
import java.time.Instant;

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

    public Long getFiles() {
        return this.files;
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

    public void setSize(Long gatherSize) {
        this.size = gatherSize;
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
        if (getTime() == null) return null;
        return Duration.ofSeconds(getTime()).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public void setTime(Long gatherTime) {
        this.time = gatherTime;
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
}
