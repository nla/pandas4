package pandas.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pandas.core.Organisation;

import java.util.List;

public interface ContactPersonRepository extends CrudRepository<ContactPerson, Long> {
    @Query("""
            select person from ContactPerson person
            join person.role as role
            where role.organisation = :organisation
              and role.type = 'contact'
              order by person.nameGiven, person.nameFamily""")
    List<ContactPerson> findByOrganisation(@Param("organisation") Organisation organisation);
}
