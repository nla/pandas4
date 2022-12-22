package pandas.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.collection.Format;
import pandas.collection.FormatRepository;
import pandas.collection.Status;
import pandas.collection.StatusRepository;
import pandas.gather.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Stream;

@Component
public class DatabaseInit {
    private final Logger log = LoggerFactory.getLogger(DatabaseInit.class);

    private final AgencyRepository agencyRepository;
    private final FormatRepository formatRepository;
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;
    private final ScopeRepository scopeRepository;
    private final StateRepository stateRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;
    private final FlywayConfig flywayConfig;

    public DatabaseInit(AgencyRepository agencyRepository, FormatRepository formatRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, ScopeRepository scopeRepository, StateRepository stateRepository, StatusRepository statusRepository, UserRepository userRepository, FlywayConfig flywayConfig) {
        this.agencyRepository = agencyRepository;
        this.formatRepository = formatRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.scopeRepository = scopeRepository;
        this.stateRepository = stateRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
        this.flywayConfig = flywayConfig;
    }

    @PostConstruct
    public void init() {
        if (formatRepository.count() == 0) {
            log.info("Populating format table");
            formatRepository.saveAll(Stream.of("Mono", "Serial", "Integrating").map(Format::new).toList());
        }

        if (gatherMethodRepository.count() == 0) {
            log.info("Populating gather_method table");
            gatherMethodRepository.saveAll(Stream.of(GatherMethod.HTTRACK, GatherMethod.UPLOAD, GatherMethod.HERITRIX,
                    GatherMethod.BROWSERTRIX).map(GatherMethod::new).toList());
        }

        if (gatherScheduleRepository.count() == 0) {
            log.info("Populating gather_schedule table");
            gatherScheduleRepository.saveAll(List.of(
                    new GatherSchedule("None", 0, 0, 0),
                    new GatherSchedule("Daily", 0, 0, 1),
                    new GatherSchedule("Monthly", 0, 1, 0),
                    new GatherSchedule("Annual", 1, 0, 0)
            ));
        }

        if (scopeRepository.count() == 0) {
            log.info("Populating scope table");
            scopeRepository.saveAll(List.of(new Scope("All pages on this website", null),
                    new Scope("Just this page", 0),
                    new Scope("This page and the pages it links to", 1)));
        }

        if (stateRepository.count() == 0) {
            log.info("Populating state table");
            stateRepository.saveAll(Stream.of("archived", "awaitGather", "checked", "checking", "creation", "deleted",
                    "deleting", "gatherPause", "gatherProcess", "gathered", "reserved11", "gathering", "archiving",
                    "failed").map(State::new).toList());
        }

        if (statusRepository.count() == 0) {
            log.info("Populating status table");
            statusRepository.saveAll(Stream.of("nominated", "rejected", "selected", "monitored", "permission requested",
                    "permission denied", "permission granted", "permission impossible", "reserved9", "reserved10",
                    "ceased").map(Status::new).toList());
        }

        if (agencyRepository.count() == 0) {
            log.info("Creating initial agency and admin user");
            Organisation organisation = new Organisation();
            organisation.setAlias("DA");
            organisation.setName("Default Agency");

            Agency agency = new Agency();
            agency.setOrganisation(organisation);
            agency = agencyRepository.save(agency);

            Role role = new Role();
            role.setOrganisation(agency.getOrganisation());
            role.setType(Role.TYPE_SYSADMIN);

            User user = new User();
            user.setNameGiven("Admin");
            user.setNameFamily("User");
            user.setUserid("admin");
            user.setPassword("admin");
            user.setActive(true);
            user.setRole(role);
            userRepository.save(user);
        }
    }
}
