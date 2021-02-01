package pandas.core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndividualRepository extends CrudRepository<Individual, Long> {
    @Query("select i from Individual i where i.userid is not null order by i.nameGiven, i.nameFamily")
    List<Individual> findByUseridIsNotNull();

    Individual findByUserid(String userid);
}
