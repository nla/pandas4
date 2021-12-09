package pandas.collection;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IssueGroupRepository extends CrudRepository<IssueGroup, Long> {
    @EntityGraph(attributePaths = "issues")
    List<IssueGroup> findByTepTitleOrderByOrder(Title title);
}
