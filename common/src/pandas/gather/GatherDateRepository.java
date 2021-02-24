package pandas.gather;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pandas.collection.Title;

import java.time.Instant;

@Repository
public interface GatherDateRepository extends CrudRepository<GatherDate, Long> {
    @Modifying
    @Query("delete from GatherDate gd where gd.id in (select gd2.id from GatherDate gd2 where gd2.title = ?1 and gd2.title.gather.nextGatherDate = gd.date)")
    void deleteIfNextForTitle(Title title);

    @Query("select min(gd.date) from GatherDate gd where gd.title = ?1")
    Instant findNextOneOffDateForTitle(Title title);
}
