package pandas.discovery;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;

import java.util.List;

@Repository
public interface RejectedDomainRepository extends CrudRepository<RejectedDomain, Long> {
    int deleteByDomainAndAgency(String domain, Agency agency);

    @Query("select d from RejectedDomain d where d.domain in ?1 and (d.agency = ?2 or d.agency is null)")
    List<RejectedDomain> findRejectedDomains(List<String> domains, Agency agency);
}
