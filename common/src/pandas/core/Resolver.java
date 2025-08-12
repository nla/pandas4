package pandas.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class Resolver {
    @PersistenceContext
    private final EntityManager entityManager;

    public Resolver(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> T ref(Class<T> type, Long id) {
        return id == null ? null : entityManager.getReference(type, id);
    }
}
