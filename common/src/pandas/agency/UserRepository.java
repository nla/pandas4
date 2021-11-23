package pandas.agency;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findByActiveIsTrueOrderByNameGivenAscNameFamilyAsc();

    Optional<User> findByUserid(String userid);

    @Query("""
            select user from User user
            join user.role as role
            where role.organisation.agency = :agency
              and role.type in ('SysAdmin', 'PanAdmin', 'AgAdmin', 'StdUser', 'InfoUser', 'SuppUser')
              and user.active = true
            order by user.nameGiven, user.nameFamily""")
    List<User> findActiveUsersByAgency(@Param("agency") Agency agency);

    @Query("""
            select user from User user
            join user.role as role
            where role.organisation.agency = :agency
              and role.type in ('SysAdmin', 'PanAdmin', 'AgAdmin', 'StdUser', 'InfoUser', 'SuppUser')
              order by user.nameGiven, user.nameFamily""")
    List<User> findUsersByAgency(@Param("agency") Agency agency);
}
