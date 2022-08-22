package pandas.collection;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectionStatsService {
    @PersistenceContext
    private EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    public Map<Long, Long> countDescendentTitlesOfChildren(long collectionId) {
        String sql = """
                with recursive descendents(COL_ID, ROOT_COL_ID) as
                         (select COL_ID, COL_ID from COL
                          where COL_PARENT_ID = :collectionId
                          union all
                          select c.COL_ID, d.ROOT_COL_ID
                          from COL c join descendents d on c.COL_PARENT_ID = d.COL_ID),
                     descendent_counts(COL_ID, ROOT_COL_ID, TITLE_COUNT) as
                         (select d.COL_ID,
                                 d.ROOT_COL_ID,
                                 (select count(*) from TITLE_COL tc where tc.COLLECTION_ID = d.COL_ID)
                          from descendents d)
                select ROOT_COL_ID as id, sum(TITLE_COUNT) as count
                from descendent_counts
                group by ROOT_COL_ID""";
        var results = entityManager.createNativeQuery(rewriteQuery(sql))
                .setParameter("collectionId", collectionId)
                .getResultList();
        return toLongLongMap(results);
    }

    @NotNull
    private Map<Long, Long> toLongLongMap(List<?> results) {
        Map<Long, Long> map = new HashMap<>();
        for (Object rawRow : results) {
            Object[] row = (Object[]) rawRow;
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    public Map<Long, Long> countDescendentTitles(List<Long> collectionIds) {
        String sql = """
                with recursive descendents(COL_ID, ROOT_COL_ID) as
                         (select COL_ID, COL_ID from COL
                          where COL_ID in :collectionIds
                          union all
                          select c.COL_ID, d.ROOT_COL_ID
                          from COL c join descendents d on c.COL_PARENT_ID = d.COL_ID),
                     descendent_counts(COL_ID, ROOT_COL_ID, TITLE_COUNT) as
                         (select d.COL_ID,
                                 d.ROOT_COL_ID,
                                 (select count(*) from TITLE_COL tc where tc.COLLECTION_ID = d.COL_ID)
                          from descendents d)
                select ROOT_COL_ID as id, sum(TITLE_COUNT) as count
                from descendent_counts
                group by ROOT_COL_ID""";
        var results = entityManager.createNativeQuery(rewriteQuery(sql))
                .setParameter("collectionIds", collectionIds)
                .getResultList();
        return toLongLongMap(results);
    }

    private String rewriteQuery(String sql) {
        if (jdbcUrl.startsWith("jdbc:oracle:")) {
            return sql.replace("with recursive", "with");
        } else {
            return sql;
        }
    }
}
