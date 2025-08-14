package pandas.gather;

import pandas.agency.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "STATE_HISTORY")
public class StateHistory {
    @Id
    @Column(name = "STATE_HISTORY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STATE_HISTORY_SEQ")
    @SequenceGenerator(name = "STATE_HISTORY_SEQ", sequenceName = "STATE_HISTORY_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name="INSTANCE_ID")
    @NotNull
    private Instance instance;

    @Column(name = "END_DATE")
    private Instant endDate;

    @ManyToOne
    @JoinColumn(name = "INDIVIDUAL_ID")
    private User user;

    @Column(name = "START_DATE")
    @NotNull
    private Instant startDate;

    @Column(name = "STATE_ID")
    @NotNull
    private State state;

    protected StateHistory() {
    }

    public StateHistory(Instance instance, State state, Instant startDate, User user) {
        this.instance = instance;
        this.state = state;
        this.startDate = startDate;
        this.user = user;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public User getUser() {
        return user;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public Long getId() {
        return this.id;
    }

    public Instance getInstance() {
        return instance;
    }

    public State getState() {
        return state;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
}
