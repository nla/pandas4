package pandas.admin.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends CrudRepository<Title,Long> {
    Title findByPi(long pi);
}
