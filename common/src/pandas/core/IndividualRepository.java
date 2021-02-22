package pandas.core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndividualRepository extends CrudRepository<Individual, Long> {
    @Query("select i from Individual i where i.userid is not null and i.active = true order by i.nameGiven, i.nameFamily")
    List<Individual> findByUseridIsNotNull();

    Optional<Individual> findByUserid(String userid);
}
