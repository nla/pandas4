package pandas.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialTargetRepository extends CrudRepository<SocialTarget, Long> {
    List<SocialTarget> findByOrderByQueryAsc();
    List<SocialTarget> findByTitle(Title title);
    public List<SocialTarget> findByServerAndQuery(String server, String query);
}
