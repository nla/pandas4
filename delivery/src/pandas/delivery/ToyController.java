package pandas.delivery;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.Title;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;

@Controller
public class ToyController {
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/test")
    @ResponseBody
    public Object test() {
        AggregationKey<Map<Long, Long>> countsByCollectionId = AggregationKey.of("countsByCollectionId");
        var result = Search.session(entityManager).search(Title.class)
                .where((f, b) -> b.must(f.match().field("collectionAncestry.id").matching(13468L))
                        .must(f.match().field("deliverable").matching(true)))
                .aggregation(countsByCollectionId, f -> f.terms().field("collectionAncestry.id", Long.class))
                .fetch(0);
        System.out.println(result.total().hitCount());
        return result.aggregation(countsByCollectionId);
    }
}
