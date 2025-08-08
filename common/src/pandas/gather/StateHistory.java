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

    @ManyToOne
    @JoinColumn(name = "STATE_ID")
    @NotNull
    private State state;

    public Instant getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long stateHistoryId) {
        this.id = stateHistoryId;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
