package pandas.gather;

import jakarta.persistence.*;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

@Entity
@Table(name = "STATE")
public class State {
    public static final String ARCHIVING = "archiving", ARCHIVED = "archived", CHECKED = "checked",
            CHECKING = "checking", CREATION = "creation", DELETED = "deleted", DELETING = "deleting",
            AWAIT_GATHER = "awaitGather", GATHERING = "gathering", GATHER_PAUSE = "gatherPause",
            GATHER_PROCESS = "gatherProcess", GATHER_STOP = "gatherStop", GATHERED = "gathered",
            PUBLISHED = "published", FAILED = "failed";

    public static final long ARCHIVED_ID = 1L;
    public static final long CREATION_ID = 5;
    public static final long GATHER_PROCESS_ID = 9;
    public static final long ARCHIVING_ID = 13;

    @Id
    @Column(name = "STATE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STATE_SEQ")
    @SequenceGenerator(name = "STATE_SEQ", sequenceName = "STATE_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "STATE_NAME")
    private String name;

    public State() {
    }

    public State(String name) {
        this.name = name;
    }

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

    public boolean isDeletedOrDeleting() {
        return getName().equals(DELETED) || getName().equals(DELETING);
    }

    public boolean isFailed() {
        return getName().equals(FAILED);
    }

    public boolean isArchived() {
        return getName().equals(ARCHIVED);
    }

    public boolean isArchivedOrArchiving() {
        return getName().equals(ARCHIVING) || isArchived();
    }

    public boolean canBeRetried() {
        return switch (getName()) {
            case ARCHIVING, DELETING, GATHER_PROCESS, GATHERING -> true;
            default -> false;
        };
    }

    public boolean isGatheringOrCreation() {
        return getName().equals(GATHERING) || getName().equals(CREATION);
    }

    public boolean isGathered() {
        return getName().equals(GATHERED);
    }

    public boolean isGathering() {
        return getName().equals(GATHERING);
    }

    public boolean isCreation() {
        return getName().equals(CREATION);
    }
}
