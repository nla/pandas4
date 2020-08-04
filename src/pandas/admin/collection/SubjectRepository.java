package pandas.admin.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface SubjectRepository extends CrudRepository<Subject, Long> {

    List<Subject> findAllByOrderByName();

    List<Subject> findByParentIsNullOrderByName();

    @Query("select distinct s from Subject s " +
            "left join fetch s.children c " +
            "where s.parent is null order by s.name")
    @QueryHints({@QueryHint(name = org.hibernate.jpa.QueryHints.HINT_PASS_DISTINCT_THROUGH, value = "false")})
    List<Subject> topTwoLevels();
}
