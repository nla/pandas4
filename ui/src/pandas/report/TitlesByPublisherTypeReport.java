package pandas.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report 5: published titles per agency, broken down by publisher type. In numbers-only mode a single
 * summary table of counts per agency is shown; with details, a per-agency listing of each title.
 */
@Component
public class TitlesByPublisherTypeReport implements ReportDefinition {
    private final EntityManager em;
    private final ReportSupport support;

    public TitlesByPublisherTypeReport(EntityManager em, ReportSupport support) {
        this.em = em;
        this.support = support;
    }

    @Override
    public String slug() {
        return "titles-by-publisher-type";
    }

    @Override
    public String name() {
        return "Archived Titles by Publisher Type";
    }

    @Override
    public String description() {
        return "Published titles per agency, profiled by publisher type.";
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

    @Override
    public boolean hasDetails() {
        return true;
    }

    private record TitleRow(long agencyId, Long titleId, String name, Long pi, String pubType, String pubName) {
    }

    @Override
    public ReportView generate(ReportParams params) {
        Map<Long, String> agencies = support.agencies(params.agencyId());

        // When a period is given, restrict to titles published (TEP displayed) within that range.
        boolean period = params.hasPeriod();
        String jpql = "select t.agency.id, t.id, t.name, t.pi, pt.name, po.name "
                + "from Title t "
                + "left join t.publisher p "
                + "left join p.type pt "
                + "left join p.organisation po "
                + "where t.tep is not null "
                + (period ? "and t.tep.displayDate >= :start and t.tep.displayDate < :end " : "")
                + (params.publisherTypeId() != null ? "and pt.id = :pubType " : "")
                + "order by t.agency.id, pt.id, t.name";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        if (period) {
            query.setParameter("start", params.startOrEpoch());
            query.setParameter("end", params.endOrNow());
        }
        if (params.publisherTypeId() != null) query.setParameter("pubType", params.publisherTypeId());

        Map<Long, List<TitleRow>> byAgency = new LinkedHashMap<>();
        for (Object[] r : query.getResultList()) {
            long agencyId = (Long) r[0];
            byAgency.computeIfAbsent(agencyId, k -> new ArrayList<>()).add(new TitleRow(
                    agencyId, (Long) r[1], (String) r[2], (Long) r[3], (String) r[4], (String) r[5]));
        }

        if (!params.showDetails()) {
            List<Row> rows = new ArrayList<>();
            long total = 0;
            for (var entry : agencies.entrySet()) {
                int count = byAgency.getOrDefault(entry.getKey(), List.of()).size();
                total += count;
                rows.add(Row.of(Cell.text(entry.getValue()), Cell.number(count)));
            }
            rows.add(Row.total(Cell.text("Total"), Cell.number(total)));
            return new ReportView(name(), params.periodSubheading(),
                    List.of(new Section(null, new Table(List.of("Agency", "Published Titles"), rows))));
        }

        List<Section> sections = new ArrayList<>();
        for (var entry : agencies.entrySet()) {
            List<TitleRow> titles = byAgency.getOrDefault(entry.getKey(), List.of());
            if (titles.isEmpty()) continue;
            List<Row> rows = new ArrayList<>();
            for (TitleRow t : titles) {
                rows.add(Row.of(NewlyArchivedTitlesReport.titleLink(t.titleId(), t.name()),
                        NewlyArchivedTitlesReport.piLink(t.pi()),
                        Cell.text(t.pubType()), Cell.text(t.pubName())));
            }
            rows.add(Row.total(Cell.text("Total"), Cell.number(titles.size()), Cell.EMPTY, Cell.EMPTY));
            sections.add(new Section(entry.getValue() + " (" + titles.size() + ")",
                    new Table(List.of("Title", "PI", "Publisher Type", "Publisher Name"), rows)));
        }
        return new ReportView(name(), params.periodSubheading(), sections);
    }
}
