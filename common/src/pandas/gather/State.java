package pandas.gather;

import javax.persistence.*;

@Entity
@Table(name = "STATE")
public class State {
    public static final String ARCHIVING = "archiving", ARCHIVED = "archived", CHECKED = "checked",
            CHECKING = "checking", CREATION = "creation", DELETED = "deleted", DELETING = "deleting",
            AWAIT_GATHER = "awaitGather", GATHERING = "gathering", GATHER_PAUSE = "gatherPause",
            GATHER_PROCESS = "gatherProcess", GATHER_STOP = "gatherStop", GATHERED = "gathered",
            PUBLISHED = "published", FAILED = "failed";

    @Id
    @Column(name = "STATE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STATE_SEQ")
    @SequenceGenerator(name = "STATE_SEQ", sequenceName = "STATE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "STATE_NAME")
    private String name;

    public Long getId() {
        return this.id;
    }

    public void setId(Long stateId) {
        this.id = stateId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String stateName) {
        this.name = stateName;
    }
}
