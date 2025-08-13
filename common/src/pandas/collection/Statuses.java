package pandas.collection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class Statuses {
    @PersistenceContext private final EntityManager entityManager;

    public Statuses(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Status ref(long id) {
        return entityManager.getReference(Status.class, id);
    }

    public Status nominated() {
        return ref(Status.NOMINATED_ID);
    }

    public Status permissionRequested() {
        return ref(Status.PERMISSION_REQUESTED_ID);
    }

    public Status selected() {
        return ref(Status.SELECTED_ID);
    }

    public Status permissionGranted() {
        return ref(Status.PERMISSION_GRANTED_ID);
    }

    public Status permissionDenied() {
        return ref(Status.PERMISSION_DENIED_ID);
    }

    public Status permissionImpossible() {
        return ref(Status.PERMISSION_IMPOSSIBLE_ID);
    }
}
