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
           "(select count(*) from Collection c where child member of c.subjects and c.isDisplayed = true and c.parent is null)" +
           ")\n" +
           "from Subject child where child.parent = :subject\n" +
           "order by name")
    List<SubjectBrief> listChildrenForDelivery(@Param("subject") Subject subject, @Param("agency") Agency agency);

    // FIXME: this approach is horribly complex
    @Query(nativeQuery = true, value = """
        with recursive subjects(SUBJECT_ID, ROOT_SUBJECT_ID) as
                    (select SUBJECT_ID, SUBJECT_ID from SUBJECT
                     where SUBJECT_PARENT_ID = :subjectId
                     union all
                     select s.SUBJECT_ID, subjects.ROOT_SUBJECT_ID
                     from SUBJECT s
                     join subjects on s.SUBJECT_PARENT_ID = subjects.SUBJECT_ID),
                collections(COL_ID, ROOT_COL_ID, ROOT_SUBJECT_ID) as
                    (select c.COL_ID, c.COL_ID, s.ROOT_SUBJECT_ID from COL c
                     join COL_SUBS cs on c.COL_ID = cs.COL_ID
                     join subjects s on s.SUBJECT_ID = cs.SUBJECT_ID
                     where IS_DISPLAYED = 1
                     union all
                     select c.COL_ID, d.ROOT_COL_ID, d.ROOT_SUBJECT_ID
                     from COL c
                     join collections d on c.COL_PARENT_ID = d.COL_ID
                     where c.IS_DISPLAYED = 1),
             collection_counts(COL_ID, ROOT_SUBJECT_ID, TITLE_COUNT) as
                 (select d.COL_ID, d.ROOT_SUBJECT_ID,
                         (select count(*) from TITLE_COL tc
                          where tc.COLLECTION_ID = d.COL_ID
                            and exists (select i.INSTANCE_ID from INSTANCE i
                                        where i.TITLE_ID = tc.TITLE_ID
                                          and i.CURRENT_STATE_ID = 1))
                  from collections d)
                select ROOT_SUBJECT_ID as id, sum(title_count) as count
                from collection_counts
                group by ROOT_SUBJECT_ID    
""")
    List<SubjectIdWithCount> countChildrenForApi(@Param("subjectId") long subjectId);

    interface SubjectIdWithCount {
        long getId();
        long getCount();
    }

}
