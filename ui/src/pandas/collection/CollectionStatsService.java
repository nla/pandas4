package pandas.collection;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pandas.gather.InstanceGather;

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

    public Stats calculateStats(long collectionId) {
        String sql = """
                with recursive descendents(COL_ID, ROOT_COL_ID, START_DATE, END_DATE) as
                         (select COL_ID, COL_ID, START_DATE, END_DATE from COL
                          where COL_ID = :collectionId
                          union all
                          select c.COL_ID, d.ROOT_COL_ID, COALESCE(c.START_DATE, d.START_DATE), COALESCE(c.END_DATE, d.END_DATE)
                          from COL c
                          join descendents d on c.COL_PARENT_ID = d.COL_ID),
                instanceIds(INSTANCE_ID) as
                    (select distinct i.INSTANCE_ID
                     from descendents d
                     join TITLE_COL tc on tc.COLLECTION_ID = d.COL_ID
                     join INSTANCE i on tc.TITLE_ID = i.TITLE_ID
                     where i.CURRENT_STATE_ID = 1
                       and (d.START_DATE is null or i.INSTANCE_DATE >= d.START_DATE)
                       and (d.END_DATE is null or i.INSTANCE_DATE <= d.END_DATE))
                select coalesce(count(distinct i.TITLE_ID), 0) as titleCount,
                       coalesce(count(*), 0) as instanceCount,
                       coalesce(sum(ig.GATHER_SIZE), 0) as gatheredBytes,
                       coalesce(sum(ig.GATHER_FILES), 0) as gatheredFiles
                from instanceIds i2
                join INSTANCE i on i.INSTANCE_ID = i2.INSTANCE_ID
                join INS_GATHER ig on i.INSTANCE_ID = ig.INSTANCE_ID
                """;
        Object[] row = (Object[]) entityManager.createNativeQuery(rewriteQuery(sql))
                .setParameter("collectionId", collectionId)
                .getResultList().get(0);
        return new Stats(((Number) row[0]).longValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue());
    }

    public record Stats(long titleCount, long instanceCount, long gatheredBytes, long gatheredFiles) {

        public String line1() {
            return String.format("%,d titles, %,d instances", titleCount, instanceCount);
        }

        public String line2() {
            return String.format("%,d files, %s", gatheredFiles, FileUtils.byteCountToDisplaySize(gatheredBytes));
        }
    }

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
