package pandas.gather;

import pandas.collection.Title;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "GATHER_DATE")
public class GatherDate {
    @Id
    @Column(name = "GATHER_DATE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GATHER_DATE_SEQ")
    @SequenceGenerator(name = "GATHER_DATE_SEQ", sequenceName = "GATHER_DATE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "GATHER_DATE")
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "TITLE_GATHER_ID")
    private Title title;

    public GatherDate() {

    }

    public GatherDate(Title title, Instant date) {
        setTitle(title);
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

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }
}
