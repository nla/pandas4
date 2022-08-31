package pandas.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlStatsRepository extends CrudRepository<UrlStatsId, UrlStats>,
        QueryByExampleExecutor<UrlStats> {
}
