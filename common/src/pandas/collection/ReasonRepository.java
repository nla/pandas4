package pandas.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReasonRepository extends CrudRepository<Reason, Long> {
    Reason findByName(String name);
    List<Reason> findAllByOrderByName();
}
