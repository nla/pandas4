package pandas.gather;

import org.springframework.data.repository.CrudRepository;

public interface GatherScheduleRepository extends CrudRepository<GatherSchedule, Long> {
    GatherSchedule findByName(String name);
}
