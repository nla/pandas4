package pandas.agency;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgencyRepository extends PagingAndSortingRepository<Agency,Long> {
    @Query("select a from Agency a order by a.organisation.name")
    List<Agency> findAllOrdered();
}
