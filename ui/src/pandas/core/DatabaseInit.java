package pandas.core;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.collection.*;
import pandas.collection.Format;
import pandas.gather.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * Populates the database with default values if it is empty.
 */
@Component
@DependsOn("flywayConfig")
public class DatabaseInit {
    private final Logger log = LoggerFactory.getLogger(DatabaseInit.class);

    private final AgencyRepository agencyRepository;
    private final FormatRepository formatRepository;
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;
    private final PublisherTypeRepository publisherTypeRepository;
    private final ScopeRepository scopeRepository;
    private final StateRepository stateRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;

    public DatabaseInit(AgencyRepository agencyRepository, FormatRepository formatRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, PublisherTypeRepository publisherTypeRepository, ScopeRepository scopeRepository, StateRepository stateRepository, StatusRepository statusRepository, UserRepository userRepository) {
        this.agencyRepository = agencyRepository;
        this.formatRepository = formatRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.publisherTypeRepository = publisherTypeRepository;
        this.scopeRepository = scopeRepository;
        this.stateRepository = stateRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
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

        if (publisherTypeRepository.count() == 0) {
            log.info("Populating publisher_type table");
            publisherTypeRepository.saveAll(List.of(
                    new PublisherType("Government", "Government bodies and agencies", ".gov.au"),
                    new PublisherType("Organisation", "Organisations can be public or private bodies that provide non-commercial material.", ".asn.au .org .org.au"),
                    new PublisherType("Education", "Educational Institutions", ".csiro.au .edu .edu.au"),
                    new PublisherType("Commercial", "Commercial bodies provide material on a cost basis.", ".com .com.au .net .net.au"),
                    new PublisherType("Personal", "Individual", ".id.au"),
                    new PublisherType("Other", "Use when unknown", "")
            ));
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
            role.setTitle("System Administrator");

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
