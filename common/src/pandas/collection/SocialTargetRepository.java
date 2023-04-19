package pandas.collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SocialTargetRepository extends CrudRepository<SocialTarget, Long>,
        PagingAndSortingRepository<SocialTarget, Long> {
    List<SocialTarget> findByOrderByQueryAsc();
    List<SocialTarget> findByTitle(Title title);
    List<SocialTarget> findByServerAndQueryIgnoreCase(String server, String query);

    @Query("""
        select target from SocialTarget target
        where target.lastVisitedDate is null or target.lastVisitedDate < :timeCutoff
        order by target.lastVisitedDate asc
        """)
    List<SocialTarget> findArchivingCandidates(Instant timeCutoff, Pageable pageable);
}
