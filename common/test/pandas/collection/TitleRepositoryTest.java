package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

    @Test
    public void test() {
        Agency agency = new Agency();
        agencyRepository.save(agency);

        User nominator = new User(agency);
        User owner = new User(agency);
        userRepository.save(nominator);
        userRepository.save(owner);

        Instant now = Instant.now();
        var title = new Title(nominator, now);
        title.setName("test");
        title.changeStatus(Status.NOMINATED, null, nominator, now);
        title.transferOwnership(agency, owner, null, owner, now);
        titleRepository.save(title);

        var rows = titleRepository.worktrayNominated(null, owner.getId(), PageRequest.ofSize(10));
        var row = rows.getContent().get(0);
        assertEquals("test", row.getName());
        assertEquals(owner.getId(), row.getOwner().getId());
        assertEquals(nominator.getId(), row.getNominator().getId());
    }
}