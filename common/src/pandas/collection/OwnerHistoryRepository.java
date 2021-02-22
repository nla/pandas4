package pandas.collection;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OwnerHistoryRepository extends CrudRepository<OwnerHistory, Long> {
    List<OwnerHistory> findByTitleOrderByDate(Title title);
}
