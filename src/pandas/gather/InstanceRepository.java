package pandas.gather;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceRepository extends CrudRepository<Instance,Long> {
    @Query("select i from Instance i where i.state.name in ('gathering', 'gatherPause', 'gatherProcess') order by i.date")
    List<Instance> findGathering();
}
