package pandas.core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndividualRepository extends CrudRepository<Individual, Long> {
    @Query("select i from Individual i where i.userid is not null and i.active = true order by i.nameGiven, i.nameFamily")
    List<Individual> findByUseridIsNotNull();

    Optional<Individual> findByUserid(String userid);

    @Query("select r.individual from Role r where r.organisation.agency = :agency " +
            "and r.type in ('SysAdmin', 'PanAdmin', 'AgAdmin', 'StdUser', 'InfoUser', 'SuppUser') " +
            "and r.individual.active = true " +
            "order by r.individual.nameGiven, r.individual.nameFamily")
    List<Individual> findActiveUsersByAgency(@Param("agency") Agency agency);
}
