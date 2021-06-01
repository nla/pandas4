package pandas.discovery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DiscoveryRepository extends CrudRepository<Discovery, Long> {
    Optional<Discovery> findBySourceAndUrl(DiscoverySource source, String url);

    Page<Discovery> findAll(Pageable pageable);

    Slice<Discovery> findByTitleIsNullAndIdGreaterThanOrderById(long lastId, Pageable pageable);
}
