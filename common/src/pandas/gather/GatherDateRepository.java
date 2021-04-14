package pandas.gather;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatherDateRepository extends CrudRepository<GatherDate, Long> {
    @Modifying
    @Query("delete from GatherDate gd where gd.id in (select gd2.id from GatherDate gd2 where gd2.gather = ?1 and gd2.gather.nextGatherDate = gd.date)")
    void deleteIfNextForTitle(TitleGather gather);
}
