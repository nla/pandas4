package pandas.api;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pandas.collection.Title;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;

@Service
public class DeliverySearcher {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Returns a map of collection id to published title count for each subcollection descending from rootCollectionId
     * (including the root collection itself).
     */
    public Map<Long, Long> getTitleCountsForCollectionTree(long rootCollectionId) {
        AggregationKey<Map<Long, Long>> countsByCollectionId = AggregationKey.of("countsByCollectionId");
        var result = Search.session(entityManager).search(Title.class)
                .where((f, b) -> b.add(f.match().field("collectionAncestry.id").matching(rootCollectionId))
                        .add(f.match().field("hasArchivedInstances").matching(true)))
                .aggregation(countsByCollectionId, f -> f.terms().field("collectionAncestry.id", Long.class))
                .fetch(0);
        return result.aggregation(countsByCollectionId);
    }
}
