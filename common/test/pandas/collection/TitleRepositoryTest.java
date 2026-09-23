package pandas.collection;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TitleRepositoryTest {
    @Autowired
    private AgencyRepository agencyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TitleRepository titleRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void test() {
        Agency agency = new Agency();
        agencyRepository.save(agency);

        User nominator = new User(agency);
        nominator.setUserid("nominator");
        User owner = new User(agency);
        owner.setUserid("owner");
        userRepository.save(nominator);
        userRepository.save(owner);

        Instant now = Instant.now();
        var title = new Title(nominator, now);
        title.setName("test");
        title.changeStatus(Status.NOMINATED, null, nominator, now);
        title.transferOwnership(agency, owner, null, owner, now);
        titleRepository.save(title);

        var rows = titleRepository.worktrayNominated(null, PageRequest.ofSize(10));
        var row = rows.getContent().get(0);
        assertEquals("test", row.getName());
        assertEquals(owner.getId(), row.getOwner().getId());
        assertEquals(nominator.getId(), row.getNominator().getId());
    }

    @Test
    void worktrayResultAndCountIncludeTitlesWithoutOwnerHistory() {
        Agency agency = new Agency();
        agencyRepository.save(agency);

        User nominator = new User(agency);
        nominator.setUserid("historyless-nominator");
        userRepository.save(nominator);

        Instant now = Instant.now();
        var title = new Title(nominator, now);
        title.setName("Nomination without owner history");
        title.changeStatus(Status.NOMINATED, null, nominator, now);
        titleRepository.save(title);
        entityManager.flush();

        entityManager.createNativeQuery("delete from owner_history where title_id = :titleId")
                .setParameter("titleId", title.getId())
                .executeUpdate();
        entityManager.clear();

        var rows = titleRepository.worktrayNominated(null, PageRequest.ofSize(5));
        assertEquals(1, rows.getContent().size());
        assertEquals(1, rows.getTotalElements());
        assertNull(rows.getContent().get(0).getNominator());
    }
}
