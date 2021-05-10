package pandas.collection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface StatusHistoryRepository extends CrudRepository<StatusHistory, Long> {
    @Modifying
    @Query("update StatusHistory sh set sh.endDate = :endDate where sh.title = :title and sh.endDate is null")
    void markPreviousEnd(@Param("title") Title title, @Param("endDate") Instant endDate);
}
