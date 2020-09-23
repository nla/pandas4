package pandas.agency;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyRepository extends PagingAndSortingRepository<Agency,Long> {
}
