package pandas.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;

import java.util.List;

@Repository
public interface SubjectRepository extends CrudRepository<Subject, Long> {

    List<Subject> findAllByOrderByName();

    List<Subject> findByParentIsNullOrderByName();

    record SubjectListItem(long id, Long parentId, String name, boolean hasIcon,
                           long titleCount, long collectionCount) {}

    @Query("""
        select new pandas.collection.SubjectRepository$SubjectListItem(
            s.id, p.id, s.name, (s.icon is not null), size(s.titles), size(s.collections))
        from Subject s
        left join s.parent p
        where s.parent is null or p.parent is null
        order by s.name""")
    List<SubjectListItem> topTwoLevels();

    @Query("""
        select new pandas.collection.SubjectRepository$SubjectListItem(
            s.id, s.parent.id, s.name, (s.icon is not null), size(s.titles), size(s.collections))
        from Subject s
        where s.parent.id = :parentId
        order by s.name""")
    List<SubjectListItem> listSubcategories(@Param("parentId") long parentId);

    @Query("select new pandas.collection.SubjectBrief(child.id, child.name,\n" +
           "(select count(*) from Title t where child member of t.subjects and (t.agency = :agency or :agency is null) and " +
           TitleRepository.SUBJECT_CONDITIONS + "), " +
           "(select count(*) from Collection c where child member of c.subjects and c.isDisplayed = true and c.parent is null)" +
           ")\n" +
           "from Subject child where child.parent = :subject\n" +
           "order by name")
    List<SubjectBrief> listChildrenForDelivery(@Param("subject") Subject subject, @Param("agency") Agency agency);

}
