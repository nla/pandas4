package pandas.search;

import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.orm.work.SearchIndexingPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pandas.collection.Title;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;

@Component
public class SearchScheduler {
    private static final Logger log = LoggerFactory.getLogger(SearchScheduler.class);

    private final EntityManagerFactory entityManagerFactory;

    public SearchScheduler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Scheduled(fixedDelayString = "${pandas.searchScheduler.delay:3600000}", initialDelayString = "${pandas.searchScheduler.initialDelay:3600000}")
    public synchronized void reindex() throws InterruptedException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Search.session(entityManager).massIndexer()
                    .startAndWait();
        } finally {
            entityManager.close();
        }
    }

    @Scheduled(fixedDelayString = "${pandas.searchScheduler.incremental.delay:5000}")
    public synchronized void incremental() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            SearchSession session = Search.session(entityManager);
            SearchIndexingPlan indexingPlan = session.indexingPlan();
            List<Title> hits = session.search(Title.class).where(SearchPredicateFactory::matchAll)
                    .sort(f -> f.field("lastModifiedDate").desc())
                    .fetch(1)
                    .hits();
            long lastId = -1;
            Instant lastIndexedDate = hits.isEmpty() ? Instant.parse("1987-01-01T00:00:00Z") : hits.get(0).getLastModifiedDate();

            while (true) {
                entityManager.getTransaction().begin();
                try {
                    @SuppressWarnings("unchecked")
                    List<Title> candidates = (List<Title>)entityManager.createQuery(
                            "select t from Title t where t.lastModifiedDate > :date or " +
                            "(t.lastModifiedDate = :date and t.id > :id) order by t.lastModifiedDate")
                            .setParameter("date", lastIndexedDate)
                            .setParameter("id", lastId)
                            .setMaxResults(100)
                            .getResultList();
                    if (!candidates.isEmpty()) {
                        for (Title candidate: candidates) {
                            indexingPlan.addOrUpdate(candidate);
                        }
                        entityManager.getTransaction().commit();
                        Title last = candidates.get(candidates.size() - 1);
                        lastIndexedDate = last.getLastModifiedDate();
                        lastId = last.getId();
                        log.info("Incrementally indexed {} titles (lastDate={}, lastId={})", candidates.size(), lastIndexedDate, lastId);
                    } else {
                        // no more to do
                        entityManager.getTransaction().rollback();
                        break;
                    }
                } catch (Exception e) {
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            };
        } finally {
            entityManager.close();
        }
    }
}
