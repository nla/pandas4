package pandas.agency;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.collection.Collection;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepository extends PagingAndSortingRepository<Agency, Long> {
    @Query("select a from Agency a order by a.organisation.name")
    List<Agency> findAllOrdered();

    @Query("select a from Agency a where a.organisation.alias = :alias")
    Optional<Agency> findByAlias(@Param("alias") String alias);

    @Query("""
             select 
                a.id as id, org.name as name, org.alias as alias,
                (select count(*) from Title t where t.agency = a) as titleCount, 
                (select count(*) from User u where u.role.organisation = org) as userCount
            from Agency a 
            join a.organisation as org 
            order by org.name
            """)
    List<AgencySummary> summarizeAllOrdered();

    @Query("""
        select a from Title t
        join t.agency a
        join t.collections c
        where c = :collection
        and exists (select i from Instance i where i.title = t and i.state.id = 1)
        group by a
        order by count(t) desc""")
    List<Agency> findByCollection(@Param("collection") Collection collection);
}
