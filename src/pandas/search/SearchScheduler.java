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
    private long lastId = -1;
    private Instant lastIndexedDate;

    public SearchScheduler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // do a full reindex once a day
    @Scheduled(fixedDelayString = "${pandas.searchScheduler.delay:86400000}", initialDelayString = "${pandas.searchScheduler.initialDelay:86400000}")
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
            if (lastIndexedDate == null) {
                List<Title> hits = session.search(Title.class).where(SearchPredicateFactory::matchAll)
                        .sort(f -> f.field("lastModifiedDate").desc())
                        .fetch(1)
                        .hits();
                lastIndexedDate = hits.isEmpty() ? Instant.parse("1987-01-01T00:00:00Z") : hits.get(0).getLastModifiedDate();
                lastId = hits.isEmpty() ? -1 : hits.get(0).getId();
            }

            while (true) {
                entityManager.getTransaction().begin();
                try {
                    @SuppressWarnings("unchecked")
                    List<Title> candidates = (List<Title>)entityManager.createQuery(
                            "select t from Title t where t.lastModifiedDate > :date or " +
                            "(t.lastModifiedDate = :date and t.id > :id) order by t.lastModifiedDate, t.id")
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
                        log.info("Incrementally indexed {} titles (lastLastModifiedDate={}, lastId={})", candidates.size(), lastIndexedDate, lastId);
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
