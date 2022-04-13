package pandas.gather;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.User;

import java.time.Instant;
import java.util.List;

@Repository
public interface StateHistoryRepository extends CrudRepository<StateHistory, Long> {
    List<StateHistory> findByInstanceOrderByStartDate(Instance instance);

    @Modifying
    @Query("update StateHistory sh set sh.endDate = ?2 where sh.instance.id = ?1 and sh.endDate is null")
    void markPreviousStateEnd(Long id, Instant now);


    @Query("""
        select new pandas.gather.InstanceEvent(sh.id, sh.startDate, sh.instance.id,
         sh.instance.title.id, sh.instance.title.name) from StateHistory sh
        where sh.state.name = 'archiving'
          and sh.user = :user
          and sh.startDate > :dateLimit
        order by sh.startDate desc
    """)
    List<InstanceEvent> findRecentlyArchivedBy(@Param("user") User user, @Param("dateLimit") Instant dateLimit);


}
