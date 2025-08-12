package pandas.collection;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContactMethodRepository extends CrudRepository<ContactMethod, Long> {
    List<ContactMethod> findAllByOrderByName();
}