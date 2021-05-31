package pandas.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface IssueRepository extends CrudRepository<Issue, Long> {
    @Query("select true from Issue i left join i.group g where g.tep.title = :title")
    boolean existForTitle(@Param("title") Title title);
}
