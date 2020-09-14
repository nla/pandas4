package pandas.admin.collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TitleRepository extends CrudRepository<Title,Long> {
    Title findByPi(long pi);

    @Query("select t from Title t where t.thumbnails is empty " +
            "and (t.titleUrl is not null or t.seedUrl is not null)")
    List<Title> findWithoutThumbnails(Pageable pageable);
}
