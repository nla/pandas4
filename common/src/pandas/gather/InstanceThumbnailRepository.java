package pandas.gather;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstanceThumbnailRepository extends CrudRepository<InstanceThumbnail, Long> {

    @Query("select it from InstanceThumbnail it where it.instance.title.id = :titleId\n" +
            "and it.status >= 200 and it.status <= 299\n" +
            "order by it.instance.date desc")
    List<InstanceThumbnail> findForTitleId(@Param("titleId") long titleId, Pageable pageable);
}
