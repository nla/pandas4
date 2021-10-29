package pandas.collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.core.Individual;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public interface CollectionRepository extends CrudRepository<Collection, Long> {
    List<Collection> findByParentIsNullAndSubjectsIsEmpty();

    @Query("select distinct c from Collection c join c.subjects s where s in (:subjects) order by name")
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

    List<Collection> findByCreatedByAndCreatedDateIsAfterOrderByCreatedDateDesc(Individual creator, Instant dateLimit);

    // Oracle gives an error for "select c ... group by c" type queries and we can't join a subquery
    // so as a workaround return the ids and then resolve them.
    @Query("select c.id from StatusHistory sh " +
            "join sh.title t " +
            "join t.collections c " +
            "where sh.individual = :user and " +
            "sh.status.name in ('selected', 'nominated') " +
            "group by c.id " +
            "order by MAX(sh.startDate) desc")
    List<Long> findRecentlyUsedIds(@Param("user") Individual user, Pageable pageable);

    default List<Collection> findAllByIdPreserveOrder(List<Long> ids) {
        var map = new HashMap<Long, Collection>();
        for (var entity: findAllById(ids)) {
            map.put(entity.getId(), entity);
        }
        return ids.stream().map(map::get).collect(toList());
    }

    default List<Collection> findRecentlyUsed(@Param("user") Individual user, Pageable pageable) {
        return findAllByIdPreserveOrder(findRecentlyUsedIds(user, pageable));
    }
}
