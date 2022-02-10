package pandas.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;

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

    @Query("select new pandas.collection.SubjectBrief(child.id, child.name,\n" +
           "(select count(*) from Title t where child member of t.subjects and (t.agency = :agency or :agency is null) and " +
           TitleRepository.SUBJECT_CONDITIONS + "), " +
           "(select count(*) from Collection c where child member of c.subjects and c.isDisplayed = true)" +
           ")\n" +
           "from Subject child where child.parent = :subject\n" +
           "order by name")
    List<SubjectBrief> listChildrenForDelivery(@Param("subject") Subject subject, @Param("agency") Agency agency);
}
