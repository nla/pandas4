package pandas.collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryRewriter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.util.TimeFrame;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface CollectionRepository extends CrudRepository<Collection, Long> {
    List<Collection> findByParentIsNullAndSubjectsIsEmpty();

    @Query("select distinct c from Collection c join c.subjects s where s in (:subjects) order by c.name")
    List<Collection> findByAnyOfSubjects(@Param("subjects") List<Subject> subject);

    @Query("select c from Collection c\n" +
            "where upper(c.name) like :pattern\n" +
            "and c.isDisplayed = true\n" +
            "and c.parent is null\n" +
            "order by c.name")
    List<Collection> findTopLevelDisplayableCollectionsNamedLike(@Param("pattern") String pattern, Pageable pageable);

    @Query("select c from Collection c\n" +
            "where c.name < 'A'\n" +
            "and c.isDisplayed = true\n" +
            "and c.parent is null\n" +
            "order by c.name")
    List<Collection> findTopLevelDisplayableCollectionsWithNumberNames(Pageable pageable);

    List<Collection> findByParentIsNullAndSubjectsContainsOrderByName(Subject subject);

    record CollectionListItem(long id, String name, Instant startDate, Instant endDate,
                              long titleCount, long childCount) {
        public TimeFrame timeFrame() {
            if (startDate == null && endDate == null) return null;
            return new TimeFrame(startDate, endDate);
        }
    }

    @Query("""
        select new pandas.collection.CollectionRepository$CollectionListItem(
            c.id, c.name, c.startDate, c.endDate, size(c.titles), size(c.children))
        from Collection c
        where c.parent is null
          and :subject member of c.subjects
        order by c.name""")
    List<CollectionListItem> listBySubject(@Param("subject") Subject subject);

    List<Collection> findByCreatedByAndCreatedDateIsAfterOrderByCreatedDateDesc(User creator, Instant dateLimit);

    // Oracle gives an error for "select c ... group by c" type queries and we can't join a subquery
    // so as a workaround return the ids and then resolve them.
    @Query("select c.id from StatusHistory sh " +
            "join sh.title t " +
            "join t.collections c " +
            "where sh.user = :user and " +
            "sh.status.name in ('selected', 'nominated') " +
            "group by c.id " +
            "order by MAX(sh.startDate) desc")
    List<Long> findRecentlyUsedIds(@Param("user") User user, Pageable pageable);

    default List<Collection> findAllByIdPreserveOrder(List<Long> ids) {
        var map = new HashMap<Long, Collection>();
        for (var entity: findAllById(ids)) {
            map.put(entity.getId(), entity);
        }
        return ids.stream().map(map::get).toList();
    }

    default List<Collection> findRecentlyUsed(@Param("user") User user, Pageable pageable) {
        return findAllByIdPreserveOrder(findRecentlyUsedIds(user, pageable));
    }

    @Query("select new pandas.collection.CollectionBrief(c.id, c.name, " +
           "(select count(*) from Title t, Collection c2 " +
           "where c2 member of t.collections " +
           " and (c = c2 or c2.parent = c) " +
           " and (t.agency = :agency or :agency is null) and " +
           TitleRepository.SUBJECT_CONDITIONS + ")) " +
           "from Collection c where :subject member of c.subjects and c.parent is null " +
           "order by name")
    List<CollectionBrief> listBySubjectForDelivery(@Param("subject") Subject subject, @Param("agency") Agency agency);

    @Query("select max(collection.id) from Collection collection")
    Long maxId();

    Page<Collection> findByIdBetween(long startId, long endId, Pageable pageable);

    interface CollectionCounts{
        long getId();
        long getTitleCount();
        long childCount();
    }

    @Query(value = """
        with recursive cte1(COL_ID, ROOT_COL_ID, TITLE_COUNT) as (
            select cs.COL_ID, cs.COL_ID, (select count(*) from TITLE_COL tc where tc.COLLECTION_ID = cs.COL_ID) as TITLE_COUNT
            from COL_SUBS cs
            where cs.SUBJECT_ID = :subjectId
            union all
            select c.COL_ID, d.ROOT_COL_ID, (select count(*) from TITLE_COL tc where tc.COLLECTION_ID = c.COL_ID) as TITLE_COUNT
            from COL c
            join cte1 d on c.COL_PARENT_ID = d.COL_ID)
        select ROOT_COL_ID as id, SUM(TITLE_COUNT) as titleCount, COUNT(*) - 1 as childCount
        from cte1
        group by ROOT_COL_ID
""", nativeQuery = true, queryRewriter = WithRecursiveRewriter.class)
    List<CollectionCounts> collectionCountsForSubject(@Param("subjectId") long subjectId);

    default Map<Long, CollectionCounts> collectionStatsMapForSubject(long subjectId) {
        var collectionStats = new HashMap<Long, CollectionRepository.CollectionCounts>();
        collectionCountsForSubject(subjectId)
                .forEach(count -> collectionStats.put(count.getId(), count));
        return collectionStats;
    }

    @Component
    class WithRecursiveRewriter implements QueryRewriter {

        private final boolean databaseIsOracle;

        public WithRecursiveRewriter(@Value("${spring.datasource.url}") String jdbcUrl) {
            databaseIsOracle = jdbcUrl.startsWith("jdbc:oracle:");
        }

        @Override
        public String rewrite(String query, Sort sort) {
            if (databaseIsOracle) {
                return query.replace("with recursive ", "with ");
            } else {
                return query;
            }
        }
    }
}
