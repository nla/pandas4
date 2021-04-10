package pandas.gather;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GatherScheduleRepository extends CrudRepository<GatherSchedule, Long> {
    Optional<GatherSchedule> findByName(String name);
}
