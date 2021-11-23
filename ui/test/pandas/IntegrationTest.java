package pandas;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.collection.Format;
import pandas.collection.FormatRepository;
import pandas.collection.Status;
import pandas.collection.StatusRepository;
import pandas.core.Organisation;
import pandas.core.Role;
import pandas.gather.*;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public abstract class IntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @BeforeAll
    public static void setupDatabase(@Autowired AgencyRepository agencyRepository,
                                     @Autowired FormatRepository formatRepository,
                                     @Autowired GatherMethodRepository gatherMethodRepository,
                                     @Autowired GatherScheduleRepository gatherScheduleRepository,
                                     @Autowired UserRepository userRepository,
                                     @Autowired ScopeRepository scopeRepository,
                                     @Autowired StateRepository stateRepository,
                                     @Autowired StatusRepository statusRepository) {
        if (userRepository.findByUserid("user").isPresent()) {
            return; // already setup
        }

        formatRepository.saveAll(List.of("Mono", "Serial", "Integrating").stream().map(Format::new).toList());
        gatherMethodRepository.saveAll(List.of(GatherMethod.HTTRACK, GatherMethod.UPLOAD, GatherMethod.HERITRIX).stream().map(GatherMethod::new).toList());
        gatherScheduleRepository.save(new GatherSchedule("Annual", 1, 0, 0));
        scopeRepository.saveAll(List.of(new Scope("All pages on this website", null),
                        new Scope("Just this page", 0),
                        new Scope("This page and the pages it links to", 1)));
        stateRepository.saveAll(List.of("archived", "awaitGather", "checked", "checking", "creation", "deleted",
                "deleting", "gatherPause", "gatherProcess", "gathered", "reserved11", "gathering", "archiving",
                "failed").stream().map(State::new).toList());
        statusRepository.saveAll(List.of("nominated", "rejected", "selected", "monitored", "permission requested",
                "permission denied", "permission granted", "permission impossible", "reserved9", "reserved10",
                "ceased").stream().map(Status::new).toList());

        Organisation organisation = new Organisation();
        organisation.setName("Test Agency");

        Agency agency = new Agency();
        agency.setOrganisation(organisation);
        agency = agencyRepository.save(agency);

        Role role = new Role();
        role.setOrganisation(agency.getOrganisation());
        role.setType(Role.TYPE_SYSADMIN);

        User user = new User();
        user.setUserid("user");
        user.setActive(true);
        user.setRole(role);
        userRepository.save(user);
    }
}
