package pandas.report;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.Agency;
import pandas.collection.*;
import pandas.core.Organisation;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;
import pandas.gather.State;
import pandas.gather.StateHistory;
import pandas.util.DateFormats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportCriteriaParityTest extends IntegrationTest {
    @Autowired EntityManager em;
    @Autowired NewlyArchivedTitlesReport newlyArchivedTitlesReport;
    @Autowired TitlesByPublisherTypeReport titlesByPublisherTypeReport;
    @Autowired ScheduledGathersReport scheduledGathersReport;
    @Autowired StatisticsByStatusReport statisticsByStatusReport;

    @Test
    void reportFlagsMatchPandas3() {
        assertTrue(titlesByPublisherTypeReport.hasPeriod());
        assertTrue(titlesByPublisherTypeReport.hasAgency());
        assertTrue(titlesByPublisherTypeReport.hasPublisherType());
        assertTrue(titlesByPublisherTypeReport.hasDetails());

        assertTrue(scheduledGathersReport.hasPeriod());
        assertTrue(scheduledGathersReport.hasAgency());
        assertTrue(scheduledGathersReport.hasDetails());
    }

    @Test
    @Transactional
    void newlyArchivedUsesCurrentCalendarYearDefaultAndOnlyRequiresPublisherForTypeFilter() {
        Agency agency = newAgency();
        PublisherType government = publisherType("Government");
        PublisherType commercial = publisherType("Commercial");
        Publisher govPublisher = publisher("Report Criteria Government Publisher", government);
        Publisher commercialPublisher = publisher("Report Criteria Commercial Publisher", commercial);
        LocalDate yearStart = LocalDate.now().withDayOfYear(1);
        Instant currentYearDisplayDate = yearStart.atStartOfDay(ZoneId.systemDefault()).plusSeconds(1).toInstant();
        Instant previousYearDisplayDate = yearStart.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        Title included = title(agency, format("Serial"), 2001L, Status.SELECTED, currentYearDisplayDate);
        included.setPublisher(govPublisher);
        displayedInstance(included, currentYearDisplayDate);

        Title noPublisher = title(agency, format("Serial"), 2002L, Status.SELECTED, currentYearDisplayDate);
        displayedInstance(noPublisher, currentYearDisplayDate);

        Title otherType = title(agency, format("Serial"), 2003L, Status.SELECTED, currentYearDisplayDate);
        otherType.setPublisher(commercialPublisher);
        displayedInstance(otherType, currentYearDisplayDate);

        Title previousYear = title(agency, format("Serial"), 2004L, Status.SELECTED, previousYearDisplayDate);
        previousYear.setPublisher(govPublisher);
        displayedInstance(previousYear, previousYearDisplayDate);
        em.flush();
        em.clear();

        ReportView unfiltered = newlyArchivedTitlesReport.generate(new ReportParams(agency.getId(),
                null, null, false, null, null));
        assertEquals(1, unfiltered.sections().size());
        assertTrue(unfiltered.subheading().startsWith(DateFormats.SHORT_DATE.format(yearStart) + " to "));
        assertEquals(List.of("Report Criteria Title 2001", "Report Criteria Title 2002", "Report Criteria Title 2003"),
                dataRows(unfiltered.sections().get(0).tables().get(0)).stream()
                        .map(row -> row.cells().get(0).csv())
                        .toList());

        ReportView filtered = newlyArchivedTitlesReport.generate(new ReportParams(agency.getId(),
                null, null, false, government.getId(), null));
        assertEquals("Newly Archived Government Titles", filtered.title());
        assertEquals(List.of("Report Criteria Title 2001"),
                dataRows(filtered.sections().get(0).tables().get(0)).stream()
                        .map(row -> row.cells().get(0).csv())
                        .toList());
    }

    @Test
    @Transactional
    void publisherTypeReportUsesLegacyOrCanonicalTepAndInnerPublisherJoins() {
        Agency agency = agency();
        PublisherType government = publisherType("Government");
        Publisher publisher = publisher("Report Criteria Publisher Type Publisher", government);
        Instant date = Instant.parse("2026-01-10T00:00:00Z");

        Title included = title(agency, format("Serial"), 2101L, Status.SELECTED, date);
        included.setPublisher(publisher);
        legacyTep(included, date);

        Title inverseTepOnly = title(agency, format("Serial"), 2102L, Status.SELECTED, date);
        inverseTepOnly.setPublisher(publisher);
        inverseTepOnly.getTep().setDisplayDate(date);

        Title noPublisher = title(agency, format("Serial"), 2103L, Status.SELECTED, date);
        legacyTep(noPublisher, date);

        Title outOfPeriod = title(agency, format("Serial"), 2104L, Status.SELECTED, date);
        outOfPeriod.setPublisher(publisher);
        legacyTep(outOfPeriod, Instant.parse("1999-01-10T00:00:00Z"));
        em.flush();
        em.clear();

        ReportView view = titlesByPublisherTypeReport.generate(new ReportParams(agency.getId(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), true, null, null));

        assertEquals(1, view.sections().size());
        assertEquals(List.of("Report Criteria Title 2101", "Report Criteria Title 2102"),
                dataRows(view.sections().get(0).tables().get(0)).stream()
                        .map(row -> row.cells().get(0).csv())
                        .toList());
    }

    @Test
    @Transactional
    void statisticsByStatusUsesPandas3StatusCountingCriteria() {
        Agency agency = agency();
        Format serial = format("Serial");
        Instant date = Instant.parse("2026-01-10T00:00:00Z");

        Title duplicateHistory = title(agency, serial, 2201L, Status.SELECTED, date);
        em.persist(new StatusHistory(duplicateHistory, Status.SELECTED, null, date.plusSeconds(1), null));
        title(agency, serial, null, Status.SELECTED, date);
        title(agency, serial, null, Status.SELECTED, date);
        title(agency, null, 2202L, Status.SELECTED, date);
        title(agency, format("Map"), 2203L, Status.SELECTED, date);
        Title canonicalTep = title(agency, serial, 2204L, Status.SELECTED, Instant.parse("2025-01-10T00:00:00Z"));
        canonicalTep.getTep().setDisplayDate(date);
        Title archived = title(agency, serial, 2205L, Status.SELECTED, Instant.parse("2025-01-10T00:00:00Z"));
        Instance archivedInstance = new Instance(archived, date, GatherMethod.HERITRIX);
        archivedInstance.changeState(State.ARCHIVED, null, date);
        em.persist(archivedInstance);
        em.persist(new StateHistory(archivedInstance, State.ARCHIVED, date.plusSeconds(1), null));
        em.flush();
        em.clear();

        ReportView view = statisticsByStatusReport.generate(new ReportParams(agency.getId(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), false, null, null));

        assertEquals(1, view.sections().size());
        List<Table> tables = view.sections().get(0).tables();
        assertEquals(List.of("Type vs Status", "Collection", "Publisher Contact", "New Archiving", "Re-archiving", "Processing"),
                List.of("Type vs Status", tables.get(1).columns().get(0), tables.get(2).columns().get(0),
                        tables.get(3).columns().get(0), tables.get(4).columns().get(0), tables.get(5).columns().get(0)));
        assertEquals(List.of("Serial", "3", "0", "0", "3"), row(tables.get(0), "Serial"));
        assertEquals(List.of("Total", "5", "0", "0", "5"), row(tables.get(0), "Total"));
        assertEquals(List.of("New titles successfully archived", "1"), row(tables.get(3), "New titles successfully archived"));
        assertEquals(List.of("Serial instances processed", "1"), row(tables.get(5), "Serial instances processed"));
        assertEquals(List.of("All instances processed", "1"), row(tables.get(5), "All instances processed"));
    }

    private Agency agency() {
        return em.createQuery("""
                        select a from Agency a
                        where a.id not in (3, 4, 13)
                        order by a.id
                        """, Agency.class)
                .setMaxResults(1)
                .getSingleResult();
    }

    private Agency newAgency() {
        Agency agency = new Agency();
        agency.getOrganisation().setName("Report Criteria Agency " + System.nanoTime());
        em.persist(agency);
        em.flush();
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

    private Title title(Agency agency, Format format, Long pi, Status status, Instant statusDate) {
        Title title = new Title();
        title.setName("Report Criteria Title " + pi);
        title.setPi(pi);
        title.setRegDate(statusDate);
        title.setSeedUrl("https://example.org/" + pi);
        title.setTitleUrl("https://example.org/" + pi);
        title.setFormat(format);
        ReflectionTestUtils.setField(title, "agency", agency);
        title.changeStatus(status, null, null, statusDate);
        em.persist(title);
        return title;
    }

    private void displayedInstance(Title title, Instant date) {
        Instance instance = new Instance(title, date, GatherMethod.HERITRIX);
        instance.setIsDisplayed(true);
        em.persist(instance);
    }

    private void legacyTep(Title title, Instant date) {
        Tep tep = new Tep(title);
        tep.setDisplayDate(date);
        em.persist(tep);
        title.setLegacyTepRelation(tep);
    }

    private List<Row> dataRows(Table table) {
        return table.rows().stream()
                .filter(row -> !row.total())
                .toList();
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
