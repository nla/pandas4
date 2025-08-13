package pandas.collection;

import jakarta.validation.constraints.NotNull;
import pandas.agency.User;

import jakarta.persistence.*;

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
    private User user;

    @ManyToOne
    @JoinColumn(name="REASON_ID")
    private Reason reason;

    @Column(name = "START_DATE")
    private Instant startDate;

    @Column(name = "END_DATE")
    private Instant endDate;

    public StatusHistory() {
    }

    public StatusHistory(Title title, @NotNull Status status, Reason reason, Instant startDate, User user) {
        this.title = title;
        this.status = status;
        this.reason = reason;
        this.startDate = startDate;
        this.user = user;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Long getId() {
        return id;
    }

    public Title getTitle() {
        return title;
    }

    public Status getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public Reason getReason() {
        return reason;
    }
}
