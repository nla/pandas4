package pandas.gather;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "GATHER_DATE")
public class GatherDate implements Comparable<GatherDate> {
    @Id
    @Column(name = "GATHER_DATE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GATHER_DATE_SEQ")
    @SequenceGenerator(name = "GATHER_DATE_SEQ", sequenceName = "GATHER_DATE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "GATHER_DATE")
    @NotNull
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "TITLE_GATHER_ID")
    @NotNull
    private TitleGather gather;

    public GatherDate() {

    }

    public GatherDate(TitleGather gather, Instant date) {
        setGather(gather);
        setDate(date);
    }

    public Instant getDate() {
        return this.date;
    }

    public void setDate(Instant gatherDate) {
        this.date = gatherDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long gatherDateId) {
        this.id = gatherDateId;
    }

    public TitleGather getGather() {
        return gather;
    }

    public void setGather(TitleGather gather) {
        this.gather = gather;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatherDate that = (GatherDate) o;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public int compareTo(GatherDate o) {
        return date.compareTo(o.date);
    }
}
