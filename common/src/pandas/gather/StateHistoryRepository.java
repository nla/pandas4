package pandas.gather;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface StateHistoryRepository extends CrudRepository<StateHistory, Long> {
    List<StateHistory> findByInstanceOrderByStartDate(Instance instance);

    @Modifying
    @Query("update StateHistory sh set sh.endDate = ?2 where sh.instance.id = ?1 and sh.endDate is null")
    void markPreviousStateEnd(Long id, Instant now);
}
