package pandas.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;
import pandas.util.DateFormats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report 4: titles that had a new displayed instance during the period, listed per agency. Optionally
 * restricted to a publisher type (e.g. Government).
 *
 * <p>Uses JPQL rather than native SQL so that the boolean {@code is_displayed} filter is emitted with
 * the right literal for each database dialect (and works against the H2 test schema).
 */
@Component
public class NewlyArchivedTitlesReport implements ReportDefinition {
    private final EntityManager em;
    private final ReportSupport support;

    public NewlyArchivedTitlesReport(EntityManager em, ReportSupport support) {
        this.em = em;
        this.support = support;
    }

    @Override
    public String slug() {
        return "newly-archived-titles";
    }

    @Override
    public String name() {
        return "Newly Archived Titles";
    }

    @Override
    public String description() {
        return "Titles with a newly displayed instance in the period, per agency.";
    }

    @Override
    public boolean hasPeriod() {
        return true;
    }

    @Override
    public boolean hasAgency() {
        return true;
    }

    @Override
    public boolean hasPublisherType() {
        return true;
    }

    private record TitleRow(long agencyId, Long titleId, String name, Long pi, LocalDate displayDate) {
    }

    @Override
    public ReportView generate(ReportParams params) {
        Map<Long, String> agencies = support.agencies(params.agencyId());

        LocalDate startDate = params.periodStart() != null ? params.periodStart() : LocalDate.now().withDayOfYear(1);
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        String jpql = "select t.agency.id, t.id, t.name, t.pi, max(i.date) "
                + "from Instance i join i.title t left join t.publisher p left join p.type pt "
                + "where i.isDisplayed = true and i.date >= :start and i.date < :end "
                + (params.publisherTypeId() != null ? "and pt.id = :pubType " : "")
                + "group by t.agency.id, t.id, t.name, t.pi order by t.agency.id, t.name";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                .setParameter("start", start)
                .setParameter("end", params.endOrNow());
        if (params.publisherTypeId() != null) query.setParameter("pubType", params.publisherTypeId());

        Map<Long, List<TitleRow>> byAgency = new LinkedHashMap<>();
        for (Object[] r : query.getResultList()) {
            long agencyId = (Long) r[0];
            Instant displayDate = (Instant) r[4];
            byAgency.computeIfAbsent(agencyId, k -> new ArrayList<>()).add(new TitleRow(
                    agencyId, (Long) r[1], (String) r[2], (Long) r[3],
                    displayDate == null ? null : LocalDate.ofInstant(displayDate, ZoneId.systemDefault())));
        }

        List<Section> sections = new ArrayList<>();
        for (var entry : agencies.entrySet()) {
            List<TitleRow> titles = byAgency.getOrDefault(entry.getKey(), List.of());
            if (titles.isEmpty()) continue;
            List<Row> rows = new ArrayList<>();
            for (TitleRow t : titles) {
                rows.add(Row.of(titleLink(t.titleId(), t.name()), piLink(t.pi()), Cell.date(t.displayDate())));
            }
            rows.add(Row.total(Cell.text("Total"), Cell.number(titles.size()), Cell.EMPTY));
            sections.add(new Section(entry.getValue() + " (" + titles.size() + ")",
                    new Table(List.of("Title", "URI", "Display Date"), rows)));
        }
        return new ReportView(title(params), periodSubheading(params, startDate), sections);
    }

    private String title(ReportParams params) {
        if (params.publisherTypeId() == null) return name();
        String type = em.createQuery("select pt.name from PublisherType pt where pt.id = :id", String.class)
                .setParameter("id", params.publisherTypeId())
                .getResultStream()
                .findFirst()
                .orElse("");
        return "Newly Archived " + type + " Titles";
    }

    private static String periodSubheading(ReportParams params, LocalDate startDate) {
        String from = DateFormats.SHORT_DATE.format(startDate);
        String to = params.periodEnd() == null ? "now" : DateFormats.SHORT_DATE.format(params.periodEnd());
        return from + " to " + to;
    }

    static Cell piLink(Long pi) {
        if (pi == null) return Cell.EMPTY;
        return Cell.link("http://nla.gov.au/nla.arc-" + pi, "nla.arc-" + pi);
    }

    /** A title name linked to its TitleView screen (rendered relative to the app context path). */
    static Cell titleLink(Long id, String name) {
        if (id == null) return Cell.text(name);
        return Cell.link("/titles/" + id, name);
    }
}
