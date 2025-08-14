package pandas.gather;

import jakarta.persistence.*;

@Entity
@Table(name = "STATE")
public class StateEntity {
    @Id
    @Column(name = "STATE_ID")
    private Integer id;

    @Column(name = "STATE_NAME")
    private String name;

    protected StateEntity() {
    }

    public StateEntity(State state) {
        this.id = state.id();
        this.name = state.getStateName();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer stateId) {
        this.id = stateId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String stateName) {
        this.name = stateName;
    }
}
