package pandas.gather;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import pandas.agency.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "STATE_HISTORY")
@NullMarked
public class StateHistory {
    @Id
    @Column(name = "STATE_HISTORY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STATE_HISTORY_SEQ")
    @SequenceGenerator(name = "STATE_HISTORY_SEQ", sequenceName = "STATE_HISTORY_SEQ", allocationSize = 1)
    @Nullable
    private Long id;

    @ManyToOne
    @JoinColumn(name="INSTANCE_ID", nullable = false)
    @NotNull
    private Instance instance;

    @Column(name = "END_DATE")
    @Nullable
    private Instant endDate;

    @ManyToOne
    @JoinColumn(name = "INDIVIDUAL_ID")
    @Nullable
    private User user;

    @Column(name = "START_DATE", nullable = false)
    @NotNull
    private Instant startDate;

    @Column(name = "STATE_ID", nullable = false)
    @NotNull
    private State state;

    protected StateHistory() {
    }

    public StateHistory(Instance instance, State state, Instant startDate, @Nullable User user) {
        this.instance = Objects.requireNonNull(instance);
        this.state = Objects.requireNonNull(state);
        this.startDate = Objects.requireNonNull(startDate);
        this.user = user;
    }

    @Nullable
    public Instant getEndDate() {
        return this.endDate;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    @Nullable
    public Long getId() {
        return this.id;
    }

    public Instance getInstance() {
        return instance;
    }

    public State getState() {
        return state;
    }

    public void setEndDate(@Nullable Instant endDate) {
        this.endDate = endDate;
    }
}
