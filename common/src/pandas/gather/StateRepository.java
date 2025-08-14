package pandas.gather;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StateRepository extends CrudRepository<StateEntity, Integer> {
    Optional<StateEntity> findByName(String name);

}
