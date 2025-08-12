package pandas.collection;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContactTypeRepository extends CrudRepository<ContactType, Long> {
    List<ContactType> findAllByOrderByName();
}