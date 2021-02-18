package pandas.collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TitleRepository extends CrudRepository<Title,Long> {
    @Query("select t from Title t where t.thumbnails is empty " +
            "and (t.titleUrl is not null or t.seedUrl is not null)")
    List<Title> findWithoutThumbnails(Pageable pageable);

    List<Title> findFirst100ByLastModifiedDateAfterOrderByLastModifiedDate(Instant start);

    @Query("select t from Title t where t.gather.method.name = 'Bulk' and t.gather.nextGatherDate < ?1")
    List<Title> findBulkTitles(Instant now);


    @Query("select t from Title t where t.gather.method.name = ?1 and t.gather.nextGatherDate < ?2 and " +
            "(t.gather.lastGatherDate is null or t.gather.lastGatherDate < ?3)")
    List<Title> fetchNewGathers(String gatherMethod, Instant now, Instant startOfThisMinute);
}
