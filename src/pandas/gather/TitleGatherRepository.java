package pandas.gather;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TitleGatherRepository extends CrudRepository<TitleGather, Long> {
    @Query("select tg from TitleGather tg " +
            "inner join fetch tg.title " +
            "where tg.nextGatherDate < :time and tg.title is not null " +
            "order by tg.nextGatherDate")
    List<TitleGather> findQueuedBefore(@Param("time") Instant time);

    boolean existsBySchedule(GatherSchedule schedule);
}
