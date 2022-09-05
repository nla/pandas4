package pandas.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlStatsRepository extends JpaRepository<UrlStats, UrlStatsId>,
        QueryByExampleExecutor<UrlStats> {
}
