package pandas.report;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import pandas.collection.Status;
import pandas.gather.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report 2: a per-agency activity dashboard for a reporting period. For each agency it shows selection
 * decisions broken down by format, plus a summary of collection additions, publisher-contact progress
 * and archiving throughput. Each metric is computed with a single set-based query grouped by agency,
 * rather than the per-cell queries of the PANDAS 3 engine.
 */
@Component
public class StatisticsByStatusReport implements ReportDefinition {
    private static final List<String> FORMATS = List.of("Serial", "Mono", "Integrating");
    private static final int SELECTED = 0, REJECTED = 1, MONITORED = 2;

    private final EntityManager em;
    private final ReportSupport support;

    public StatisticsByStatusReport(EntityManager em, ReportSupport support) {
        this.em = em;
        this.support = support;
    }

    @Override
    public String slug() {
        return "statistics-by-status";
    }

    @Override
    public String name() {
        return "Statistics By Status";
    }

    @Override
    public String description() {
        return "Per-agency selection decisions by format, plus collection, contact and archiving activity.";
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
    public ReportView generate(ReportParams params) {
        Map<Long, String> agencies = support.agencies(params.agencyId());
        Instant from = params.startOrEpoch(), to = params.endOrNow();

        // agency -> format -> [selected, rejected, monitored]
        Map<Long, Map<String, long[]>> selection = new LinkedHashMap<>();
        List<Object[]> selectionRows = em.createQuery("""
                select t.agency.id, f.name, sh.status, count(distinct t.id)
                from StatusHistory sh join sh.title t left join t.format f
                where sh.startDate >= :start and sh.startDate < :end
                  and sh.status in :statuses
                group by t.agency.id, f.name, sh.status
                """, Object[].class)
                .setParameter("start", from).setParameter("end", to)
                .setParameter("statuses", List.of(Status.REJECTED, Status.SELECTED, Status.MONITORED))
                .getResultList();
        for (Object[] r : selectionRows) {
            int index = switch ((Status) r[2]) {
                case SELECTED -> SELECTED;
                case REJECTED -> REJECTED;
                default -> MONITORED; // MONITORED
            };
            String fmt = (String) r[1];
            selection.computeIfAbsent((Long) r[0], k -> new LinkedHashMap<>())
                    .computeIfAbsent(fmt == null ? "Unknown" : fmt, k -> new long[3])[index] += (Long) r[3];
        }

        Map<Long, Long> collectionAdds = new LinkedHashMap<>();
        for (Object[] r : em.createQuery("""
                select t.agency.id, count(distinct t.id)
                from Title t join t.collections c
                where t.regDate >= :start and t.regDate < :end
                group by t.agency.id
                """, Object[].class).setParameter("start", from).setParameter("end", to).getResultList()) {
            collectionAdds.put((Long) r[0], (Long) r[1]);
        }

        Map<Long, long[]> contacts = new LinkedHashMap<>(); // [permission requested, permission granted]
        for (Object[] r : em.createQuery("""
                select t.agency.id, sh.status, count(distinct t.id)
                from StatusHistory sh join sh.title t
                where sh.startDate >= :start and sh.startDate < :end
                  and sh.status in :statuses
                group by t.agency.id, sh.status
                """, Object[].class)
                .setParameter("start", from).setParameter("end", to)
                .setParameter("statuses", List.of(Status.PERMISSION_REQUESTED, Status.PERMISSION_GRANTED))
                .getResultList()) {
            int index = r[1] == Status.PERMISSION_REQUESTED ? 0 : 1;
            contacts.computeIfAbsent((Long) r[0], k -> new long[2])[index] += (Long) r[2];
        }

        Map<Long, long[]> archiving = new LinkedHashMap<>(); // [titles, instances]
        for (Object[] r : em.createQuery("""
                select t.agency.id, count(distinct t.id), count(distinct i.id)
                from StateHistory sh join sh.instance i join i.title t
                where sh.state = :archived and sh.startDate >= :start and sh.startDate < :end
                group by t.agency.id
                """, Object[].class)
                .setParameter("archived", State.ARCHIVED)
                .setParameter("start", from).setParameter("end", to).getResultList()) {
            archiving.put((Long) r[0], new long[]{(Long) r[1], (Long) r[2]});
        }

        List<Section> sections = new ArrayList<>();
        for (var entry : agencies.entrySet()) {
            Long agencyId = entry.getKey();
            boolean hasData = selection.containsKey(agencyId) || collectionAdds.containsKey(agencyId)
                    || contacts.containsKey(agencyId) || archiving.containsKey(agencyId);
            if (!hasData) continue;

            List<Table> tables = new ArrayList<>();
            tables.add(selectionTable(selection.getOrDefault(agencyId, Map.of())));

            long[] contact = contacts.getOrDefault(agencyId, new long[2]);
            long[] arch = archiving.getOrDefault(agencyId, new long[2]);
            List<Row> activity = List.of(
                    Row.of(Cell.text("Titles added to a collection"), Cell.number(collectionAdds.getOrDefault(agencyId, 0L))),
                    Row.of(Cell.text("Initiated publisher contacts"), Cell.number(contact[0])),
                    Row.of(Cell.text("Permissions granted"), Cell.number(contact[1])),
                    Row.of(Cell.text("New titles archived"), Cell.number(arch[0])),
                    Row.of(Cell.text("Instances archived"), Cell.number(arch[1])));
            tables.add(new Table(List.of("Activity", "Count"), activity));

            sections.add(new Section(entry.getValue(), tables));
        }
        return new ReportView(name(), params.periodSubheading(), sections);
    }

    private Table selectionTable(Map<String, long[]> byFormat) {
        List<Row> rows = new ArrayList<>();
        long[] columnTotals = new long[3];
        for (String format : FORMATS) {
            long[] c = byFormat.getOrDefault(format, new long[3]);
            rows.add(selectionRow(format, c, false));
            for (int i = 0; i < 3; i++) columnTotals[i] += c[i];
        }
        // Include any unexpected formats (e.g. null) in the totals.
        for (var e : byFormat.entrySet()) {
            if (FORMATS.contains(e.getKey())) continue;
            for (int i = 0; i < 3; i++) columnTotals[i] += e.getValue()[i];
        }
        rows.add(selectionRow("Total", columnTotals, true));
        return new Table(List.of("", "Selected", "Rejected", "Monitored", "Total"), rows);
    }

    private static Row selectionRow(String label, long[] c, boolean total) {
        long rowTotal = c[SELECTED] + c[REJECTED] + c[MONITORED];
        return new Row(List.of(Cell.text(label), Cell.number(c[SELECTED]), Cell.number(c[REJECTED]),
                Cell.number(c[MONITORED]), Cell.number(rowTotal)), total);
    }
}
