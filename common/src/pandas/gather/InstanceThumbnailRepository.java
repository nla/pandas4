package pandas.gather;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstanceThumbnailRepository extends CrudRepository<InstanceThumbnail, InstanceThumbnailId> {

    @Query("select it from InstanceThumbnail it where it.instance.title.id = :titleId\n" +
            "and it.status >= 200 and it.status <= 299\n" +
            "order by it.instance.date desc, it.type")
    List<InstanceThumbnail> findForTitleId(@Param("titleId") long titleId, Pageable pageable);

    boolean existsByInstanceAndType(Instance instance, InstanceThumbnail.Type type);

    Optional<InstanceThumbnail> findByInstanceAndType(Instance instance, InstanceThumbnail.Type type);

}
