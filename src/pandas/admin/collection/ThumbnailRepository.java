package pandas.admin.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ThumbnailRepository extends CrudRepository<Thumbnail, Long> {
    @Query("select t from Thumbnail t where t.title.id = :titleId and t.status >= 200 order by t.priority")
    Thumbnail findFirstByTitleId(@Param("titleId") long titleId);
}
