package pandas.admin.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TitleRepository extends CrudRepository<Title,Long> {
    Title findByPi(long pi);
}
