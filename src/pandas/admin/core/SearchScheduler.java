package pandas.admin.core;

import org.hibernate.search.mapper.orm.Search;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@Component
public class SearchScheduler {
    private final EntityManagerFactory entityManagerFactory;

    public SearchScheduler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000)
    public void reindex() throws InterruptedException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Search.session(entityManager).massIndexer().startAndWait();
        } finally {
            entityManager.close();
        }
    }
}
