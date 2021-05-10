package pandas.collection;

import pandas.core.Individual;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "STATUS_HISTORY")
public class StatusHistory {
    @Id
    @Column(name = "STATUS_HISTORY_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "STATUS_HISTORY_SEQ")
    @SequenceGenerator(name = "STATUS_HISTORY_SEQ", sequenceName = "STATUS_HISTORY_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name="TITLE_ID")
    private Title title;

    @ManyToOne
    @JoinColumn(name = "STATUS_ID")
    private Status status;

    @ManyToOne
    @JoinColumn(name="INDIVIDUAL_ID")
    private Individual individual;

    @ManyToOne
    @JoinColumn(name="REASON_ID")
    private Reason reason;

    @Column(name = "START_DATE")
    private Instant startDate;

    @Column(name = "END_DATE")
    private Instant endDate;

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }
}
