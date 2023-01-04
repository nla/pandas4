package pandas.collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;
import pandas.agency.User;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

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
}
