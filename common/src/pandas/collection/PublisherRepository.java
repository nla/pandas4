package pandas.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pandas.core.Organisation;

import java.util.Optional;

@Repository
public interface PublisherRepository extends PagingAndSortingRepository<Publisher, Long> {
    Optional<Publisher> findByOrganisation(Organisation organisation);
}
