package pandas.collection;

import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.core.WithRecursiveQueryRewriter;
import pandas.util.TimeFrame;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Repository
public interface CollectionRepository extends CrudRepository<Collection, Long> {
    List<Collection> findByParentIsNullAndSubjectsIsEmpty();

    @Query("select distinct c from Collection c join c.subjects s where s in (:subjects) order by c.name")
    List<Collection> findByAnyOfSubjects(@Param("subjects") List<Subject> subject);

    @Query("select distinct c from Collection c join c.subjects s where s.id in (:subjects) order by c.name")
    List<Collection> findByAnyOfSubjectIds(@Param("subjects") List<Long> subjectIds);

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

    interface TitleCount {
        long getId();
        long getTitleCount();

        private static Map<Long, Long> toMap(List<TitleCount> list) {
            return list.stream().collect(Collectors.toMap(TitleCount::getId, TitleCount::getTitleCount));
        }
    }

    @Query(value = """
        with recursive cte1(COL_ID, ROOT_COL_ID, TITLE_COUNT) as (
            select cs.COL_ID, cs.COL_ID, (select count(*) from title_col tc where tc.COLLECTION_ID = cs.COL_ID) as TITLE_COUNT
            from col_subs cs
            where cs.SUBJECT_ID = :subjectId
            union all
            select c.COL_ID, d.ROOT_COL_ID, (select count(*) from title_col tc where tc.COLLECTION_ID = c.COL_ID) as TITLE_COUNT
            from col c
            join cte1 d on c.COL_PARENT_ID = d.COL_ID)
        select ROOT_COL_ID as id, SUM(TITLE_COUNT) as titleCount
        from cte1
        group by ROOT_COL_ID
""", nativeQuery = true, queryRewriter = WithRecursiveQueryRewriter.class)
    List<TitleCount> countTitlesForCollectionsInSubject0(@Param("subjectId") long subjectId);

    /**
     * Returns a map of collection id to title count for all collections in the given subject. The title count
     * includes titles in sub-collections.
     */
    default Map<Long, Long> countTitlesForCollectionsInSubject(long subjectId) {
        return TitleCount.toMap(countTitlesForCollectionsInSubject0(subjectId));
    }

    @Query(value = """
                with recursive descendents2(COL_ID, ROOT_COL_ID) as
                         (select COL_ID, COL_ID from col
                          where COL_PARENT_ID = :collectionId
                          union all
                          select c.COL_ID, d.ROOT_COL_ID
                          from col c join descendents2 d on c.COL_PARENT_ID = d.COL_ID),
                     descendent_counts2(COL_ID, ROOT_COL_ID, TITLE_COUNT) as
                         (select d.COL_ID,
                                 d.ROOT_COL_ID,
                                 (select count(*) from title_col tc where tc.COLLECTION_ID = d.COL_ID)
                          from descendents2 d)
                select ROOT_COL_ID as id, sum(TITLE_COUNT) as titleCount
                from descendent_counts2
                group by ROOT_COL_ID""", nativeQuery = true, queryRewriter = WithRecursiveQueryRewriter.class)
    List<TitleCount> countTitlesForChildCollections0(@Param("collectionId") long collectionId);

    /**
     * Returns a map of collection id to title count for all child collections of the given collection. The title count
     * includes titles belonging to sub-collections.
     */
    default Map<Long, Long> countTitlesForChildCollections(long collectionId) {
        return TitleCount.toMap(countTitlesForChildCollections0(collectionId));
    }

    @Query(value = """
        with recursive descendents3(COL_ID, ROOT_COL_ID) as
                 (select COL_ID, COL_ID from col
                  where COL_ID in :collectionIds
                  union all
                  select c.COL_ID, d.ROOT_COL_ID
                  from col c join descendents3 d on c.COL_PARENT_ID = d.COL_ID),
             descendent_counts3(COL_ID, ROOT_COL_ID, TITLE_COUNT) as
                 (select d.COL_ID,
                         d.ROOT_COL_ID,
                         (select count(*) from title_col tc where tc.COLLECTION_ID = d.COL_ID)
                  from descendents3 d)
        select ROOT_COL_ID as id, sum(TITLE_COUNT) as titleCount
        from descendent_counts3
        group by ROOT_COL_ID""", nativeQuery = true, queryRewriter = WithRecursiveQueryRewriter.class)
    List<TitleCount> countTitlesForCollections0(@Param("collectionIds") List<Long> collectionIds);

    /**
     * Returns a map of collection id to title count for the given collections. The title count includes titles belonging
     * to sub-collections.
     */
    default Map<Long, Long> countTitlesForCollections(List<Long> collectionIds) {
        return TitleCount.toMap(countTitlesForCollections0(collectionIds));
    }

    interface CollectionStats {
        long getTitleCount();
        long getInstanceCount();
        long getGatheredBytes();
        long getGatheredFiles();

        default String line1() {
            return String.format("%,d titles, %,d instances", getTitleCount(), getInstanceCount());
        }

        default String line2() {
            return String.format("%,d files, %s", getGatheredFiles(), FileUtils.byteCountToDisplaySize(getGatheredBytes()));
        }
    }

    @Query(value = """
         with recursive descendents1(COL_ID, ROOT_COL_ID, START_DATE, END_DATE) as
                         (select COL_ID, COL_ID, START_DATE, END_DATE from col
                          where COL_ID = :collectionId
                          union all
                          select c.COL_ID, d.ROOT_COL_ID, COALESCE(c.START_DATE, d.START_DATE), COALESCE(c.END_DATE, d.END_DATE)
                          from col c
                          join descendents1 d on c.COL_PARENT_ID = d.COL_ID),
                instanceIds(INSTANCE_ID) as
                    (select distinct i.INSTANCE_ID
                     from descendents1 d
                     join title_col tc on tc.COLLECTION_ID = d.COL_ID
                     join instance i on tc.TITLE_ID = i.TITLE_ID
                     where i.CURRENT_STATE_ID = 1
                       and (d.START_DATE is null or i.INSTANCE_DATE >= d.START_DATE)
                       and (d.END_DATE is null or i.INSTANCE_DATE <= d.END_DATE))
                select coalesce(count(distinct i.TITLE_ID), 0) as titleCount,
                       coalesce(count(*), 0) as instanceCount,
                       coalesce(sum(ig.GATHER_SIZE), 0) as gatheredBytes,
                       coalesce(sum(ig.GATHER_FILES), 0) as gatheredFiles
                from instanceIds i2
                join instance i on i.INSTANCE_ID = i2.INSTANCE_ID
                join ins_gather ig on i.INSTANCE_ID = ig.INSTANCE_ID""",
            nativeQuery = true, queryRewriter = WithRecursiveQueryRewriter.class)
    CollectionStats calculateCollectionStats(long collectionId);
}
