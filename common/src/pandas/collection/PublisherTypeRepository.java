package pandas.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherTypeRepository extends CrudRepository<PublisherType, Long>, PagingAndSortingRepository<PublisherType, Long> {
    Optional<PublisherType> findByName(String name);
}
