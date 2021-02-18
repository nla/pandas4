package pandas.gather;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StateRepository extends CrudRepository<State, Long> {
    Optional<State> findByName(String name);
}
