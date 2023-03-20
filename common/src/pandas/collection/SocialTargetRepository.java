package pandas.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialTargetRepository extends CrudRepository<SocialTarget, Long>,
        PagingAndSortingRepository<SocialTarget, Long> {
    List<SocialTarget> findByOrderByQueryAsc();
    List<SocialTarget> findByTitle(Title title);
    List<SocialTarget> findByServerAndQueryIgnoreCase(String server, String query);
}
