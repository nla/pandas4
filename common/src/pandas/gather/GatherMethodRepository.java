package pandas.gather;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GatherMethodRepository extends CrudRepository<GatherMethod, Long> {
    Optional<GatherMethod> findByName(String name);
}
