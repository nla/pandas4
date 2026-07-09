package pandas.report;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.Agency;
import pandas.collection.Format;
import pandas.collection.Publisher;
import pandas.collection.PublisherType;
import pandas.collection.Status;
import pandas.collection.StatusHistory;
import pandas.collection.Title;
import pandas.core.Organisation;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;
import pandas.gather.State;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegalDepositReportTest extends IntegrationTest {
    @Autowired EntityManager em;
    @Autowired LegalDepositReport report;
    @Autowired ReportConfig reportConfig;

    @Test
    @Transactional
    void usesPandas3CountingCriteria() {
        assertTrue(report.hasPublisherType());

        Agency agency = agency();
        Format serial = format("Serial");
        Format mono = format("Mono");
        Format integrating = format("Integrating");
        Format map = format("Map");
        PublisherType government = publisherType("Government");
        Publisher governmentPublisher = publisher("Legal Deposit Government Publisher", government);
        Publisher commercialPublisher = publisher("Legal Deposit Commercial Publisher", publisherType("Commercial"));
        Instant inPeriod = Instant.parse("2026-01-10T00:00:00Z");

        Title serialWithDuplicateHistory = title(agency, serial, 1001L, governmentPublisher, true, Status.SELECTED, inPeriod);
        em.persist(new StatusHistory(serialWithDuplicateHistory, Status.SELECTED, null, inPeriod.plusSeconds(1), null));

        title(agency, serial, null, governmentPublisher, true, Status.SELECTED, inPeriod);
        title(agency, serial, null, governmentPublisher, true, Status.SELECTED, inPeriod);
        title(agency, null, 1002L, governmentPublisher, true, Status.SELECTED, inPeriod);
        title(agency, serial, 1003L, governmentPublisher, false, Status.SELECTED, inPeriod);
        title(agency, serial, 1006L, commercialPublisher, true, Status.SELECTED, inPeriod);
        title(agency, map, 1004L, governmentPublisher, true, Status.PERMISSION_REQUESTED, inPeriod);
        title(agency, mono, 1005L, governmentPublisher, true, Status.PERMISSION_GRANTED, inPeriod);

        Title archivedWithoutPi = title(agency, integrating, null, governmentPublisher, true, Status.NOMINATED, inPeriod);
        Instance archivedInstance = new Instance(archivedWithoutPi, inPeriod, GatherMethod.HERITRIX);
        archivedInstance.changeState(State.ARCHIVED, null, inPeriod);
        em.persist(archivedInstance);

        em.flush();
        em.clear();

        ReportView view = report.generate(new ReportParams(agency.getId(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), false, government.getId(), null));

        assertEquals(1, view.sections().size());
        Table table = view.sections().get(0).tables().get(0);
        assertEquals(List.of("Serial", "3", "0", "0", "0"), row(table, "Serial"));
        assertEquals(List.of("Mono", "0", "0", "1", "0"), row(table, "Mono"));
        assertEquals(List.of("Integrating", "0", "0", "0", "1"), row(table, "Integrating"));
        assertEquals(List.of("Total", "4", "1", "1", "1"), row(table, "Total"));
    }

    private Agency agency() {
        Agency agency;
        do {
            agency = new Agency();
            Organisation organisation = new Organisation();
            organisation.setName("Legal Deposit Report Test " + System.nanoTime());
            agency.setOrganisation(organisation);
            em.persist(agency);
            em.flush();
        } while (reportConfig.isExcluded(agency.getId()));
        return agency;
    }

    private Format format(String name) {
        List<Format> existing = em.createQuery("select f from Format f where f.name = :name", Format.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();
        if (!existing.isEmpty()) return existing.get(0);
        Format format = new Format(name);
        em.persist(format);
        em.flush();
        return format;
    }

    private PublisherType publisherType(String name) {
        return em.createQuery("select pt from PublisherType pt where pt.name = :name", PublisherType.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getSingleResult();
    }

    private Publisher publisher(String name, PublisherType type) {
        Organisation organisation = new Organisation();
        organisation.setName(name);
        Publisher publisher = new Publisher();
        publisher.setOrganisation(organisation);
        publisher.setType(type);
        em.persist(publisher);
        em.flush();
        return publisher;
    }

    private Title title(Agency agency, Format format, Long pi, Publisher publisher, boolean legalDeposit, Status status,
                        Instant statusDate) {
        Title title = new Title();
        title.setName("Legal Deposit Report Test Title " + System.nanoTime());
        title.setPi(pi);
        title.setRegDate(statusDate);
        title.setSeedUrl("https://example.org/");
        title.setTitleUrl("https://example.org/");
        title.setFormat(format);
        title.setPublisher(publisher);
        title.setLegalDeposit(legalDeposit);
        ReflectionTestUtils.setField(title, "agency", agency);
        title.changeStatus(status, null, null, statusDate);
        em.persist(title);
        return title;
    }

    private List<String> row(Table table, String label) {
        return table.rows().stream()
                .filter(row -> row.cells().get(0).csv().equals(label))
                .findFirst()
                .orElseThrow()
                .cells().stream()
                .map(Cell::csv)
                .toList();
    }
}
