package pandas.gather;

import org.springframework.data.repository.CrudRepository;

public interface GatherMethodRepository extends CrudRepository<GatherMethod, Long> {
    GatherMethod findByName(String name);
}
