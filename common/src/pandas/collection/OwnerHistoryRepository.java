package pandas.collection;

import org.springframework.data.repository.CrudRepository;
import pandas.agency.User;

import java.time.Instant;
import java.util.List;

public interface OwnerHistoryRepository extends CrudRepository<OwnerHistory, Long> {
    List<OwnerHistory> findByTitleOrderByDate(Title title);
    List<OwnerHistory> findByUserAndDateAfterOrderByDateDesc(User user, Instant dateAfter);
    List<OwnerHistory> findByTransferrerAndDateAfterOrderByDateDesc(User transferrer, Instant dateAfter);
}
