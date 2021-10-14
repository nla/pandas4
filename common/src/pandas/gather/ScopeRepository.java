package pandas.gather;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScopeRepository extends CrudRepository<Scope,Long> {
}
