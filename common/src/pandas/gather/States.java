package pandas.gather;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class States {
    @PersistenceContext
    private final EntityManager entityManager;

    public States(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public State ref(long id) {
        return entityManager.getReference(State.class, id);
    }

    public State archiving() {
        return ref(State.ARCHIVING_ID);
    }

    public State creation() {
        return ref(State.CREATION_ID);
    }

    public State gatherProcess() {
        return ref(State.GATHER_PROCESS_ID);
    }
}
