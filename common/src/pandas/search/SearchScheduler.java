package pandas.search;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.orm.work.SearchIndexingPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pandas.collection.Collection;
import pandas.collection.Title;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import pandas.gather.Instance;

import java.time.Instant;
import java.util.List;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.search.enabled", havingValue = "true", matchIfMissing = true)
public class SearchScheduler {
    private static final Logger log = LoggerFactory.getLogger(SearchScheduler.class);

    private final EntityManagerFactory entityManagerFactory;
    private long lastTitleId = -1;
    private long lastInstanceId = -1;
    private Instant lastIndexedTitleDate;
    private Instant lastIndexedInstanceDate;
    private Long lastCollectionId = null;

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

    @Scheduled(fixedDelayString = "${pandas.searchScheduler.incremental.delay:5000}", initialDelayString = "${pandas.searchScheduler.incremental.initialDelay:0}")
    public synchronized void incremental() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            SearchSession session = Search.session(entityManager);
            SearchIndexingPlan indexingPlan = session.indexingPlan();
            incrementallyIndexTitles(entityManager, session, indexingPlan);
            incrementallyIndexCollections(entityManager, session, indexingPlan);
            incrementallyIndexInstances(entityManager, session, indexingPlan);
        } finally {
            entityManager.close();
        }
    }

    private void incrementallyIndexCollections(EntityManager entityManager, SearchSession session, SearchIndexingPlan indexingPlan) {
        if (lastCollectionId == null) {
            List<Collection> hits = session.search(Collection.class).where(SearchPredicateFactory::matchAll)
                    .sort(f -> f.field("id").desc()).fetch(1).hits();
            lastCollectionId = hits.isEmpty() ? -1 : hits.get(0).getId();
        }
        while (true) {
            entityManager.getTransaction().begin();
            try {
                @SuppressWarnings("unchecked")
                List<Collection> candidates = entityManager.createQuery("select c from Collection c where c.id > :id order by c.id")
                        .setParameter("id", lastCollectionId)
                        .setMaxResults(100)
                        .getResultList();
                if (!candidates.isEmpty()) {
                    for (Collection candidate: candidates) {
                        indexingPlan.addOrUpdate(candidate);
                    }
                    entityManager.getTransaction().commit();
                    lastCollectionId = candidates.get(candidates.size() - 1).getId();
                    log.info("Incrementally indexed {} collections (lastId={})", candidates.size(), lastCollectionId);
                } else {
                    // no more to do
                    entityManager.getTransaction().rollback();
                    break;
                }
            } catch (Exception e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
        }

    }
    private void incrementallyIndexTitles(EntityManager entityManager, SearchSession session, SearchIndexingPlan indexingPlan) {
        if (lastIndexedTitleDate == null) {
            List<Title> hits = session.search(Title.class).where(SearchPredicateFactory::matchAll)
                    .sort(f -> f.field("lastModifiedDate").desc())
                    .fetch(1)
                    .hits();
            lastIndexedTitleDate = hits.isEmpty() ? Instant.parse("1987-01-01T00:00:00Z") : hits.get(0).getLastModifiedDate();
            lastTitleId = hits.isEmpty() ? -1 : hits.get(0).getId();
        }

        while (true) {
            entityManager.getTransaction().begin();
            try {
                @SuppressWarnings("unchecked")
                List<Title> titleCandidates = (List<Title>) entityManager.createQuery(
                        "select t from Title t where t.lastModifiedDate > :date or " +
                        "(t.lastModifiedDate = :date and t.id > :id) order by t.lastModifiedDate, t.id")
                        .setParameter("date", lastIndexedTitleDate)
                        .setParameter("id", lastTitleId)
                        .setMaxResults(100)
                        .getResultList();
                if (!titleCandidates.isEmpty()) {
                    for (Title candidate: titleCandidates) {
                        indexingPlan.addOrUpdate(candidate);
                    }
                    entityManager.getTransaction().commit();
                    Title last = titleCandidates.get(titleCandidates.size() - 1);
                    lastIndexedTitleDate = last.getLastModifiedDate();
                    lastTitleId = last.getId();
                    log.info("Incrementally indexed {} titles (lastLastModifiedDate={}, lastId={})", titleCandidates.size(), lastIndexedTitleDate, lastTitleId);
                } else {
                    // no more to do
                    entityManager.getTransaction().rollback();
                    break;
                }
            } catch (Exception e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
        ;
    }

    private void incrementallyIndexInstances(EntityManager entityManager, SearchSession session, SearchIndexingPlan indexingPlan) {
        if (lastIndexedInstanceDate == null) {
            var hits = session.search(Instance.class).where(SearchPredicateFactory::matchAll)
                    .sort(f -> f.field("lastModifiedDate").desc())
                    .fetch(1)
                    .hits();
            lastIndexedTitleDate = hits.isEmpty() ? Instant.parse("1987-01-01T00:00:00Z") : hits.get(0).getLastModifiedDate();
            lastInstanceId = hits.isEmpty() ? -1 : hits.get(0).getId();
        }

        while (true) {
            entityManager.getTransaction().begin();
            try {
                @SuppressWarnings("unchecked")
                var candidates = (List<Instance>) entityManager.createQuery(
                                "select i from Instance i where i.lastModifiedDate > :date or " +
                                "(i.lastModifiedDate = :date and i.id > :id) order by i.lastModifiedDate, t.id")
                        .setParameter("date", lastIndexedTitleDate)
                        .setParameter("id", lastInstanceId)
                        .setMaxResults(100)
                        .getResultList();
                if (!candidates.isEmpty()) {
                    for (var candidate: candidates) {
                        indexingPlan.addOrUpdate(candidate);
                    }
                    entityManager.getTransaction().commit();
                    var last = candidates.get(candidates.size() - 1);
                    lastIndexedTitleDate = last.getLastModifiedDate();
                    lastInstanceId = last.getId();
                    log.info("Incrementally indexed {} instances (lastLastModifiedDate={}, lastId={})", candidates.size(), lastIndexedTitleDate, lastInstanceId);
                } else {
                    // no more to do
                    entityManager.getTransaction().rollback();
                    break;
                }
            } catch (Exception e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
        }
    }
}
