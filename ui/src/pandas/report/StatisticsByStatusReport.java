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

        Map<Long, Long> newArchiving = new LinkedHashMap<>();
        for (Object[] r : em.createQuery("""
                select t.agency.id, count(distinct t.id)
                from Title t left join t.legacyTepRelation legacyTep left join t.tep tep
                where (legacyTep is not null or tep is not null)
                  and ((legacyTep is not null and legacyTep.displayDate >= :start and legacyTep.displayDate < :end)
                    or (tep is not null and tep.displayDate >= :start and tep.displayDate < :end))
                group by t.agency.id
                """, Object[].class)
                .setParameter("start", from).setParameter("end", to).getResultList()) {
            newArchiving.put((Long) r[0], (Long) r[1]);
        }

        InstanceActivity instanceActivity = instanceActivity(from, to);

        List<Section> sections = new ArrayList<>();
        for (var entry : agencies.entrySet()) {
            Long agencyId = entry.getKey();
            boolean hasData = selection.containsKey(agencyId) || collectionAdds.containsKey(agencyId)
                    || contacts.containsKey(agencyId) || newArchiving.containsKey(agencyId)
                    || instanceActivity.hasData(agencyId);
            if (!hasData) continue;

            List<Table> tables = new ArrayList<>();
            tables.add(selectionTable(selection.getOrDefault(agencyId, Map.of())));

            long[] contact = contacts.getOrDefault(agencyId, new long[2]);
            tables.add(new Table(List.of("Collection", ""),
                    List.of(Row.of(Cell.text("Titles added to a collection"), Cell.number(collectionAdds.getOrDefault(agencyId, 0L))))));
            tables.add(new Table(List.of("Publisher Contact", ""),
                    List.of(Row.of(Cell.text("Initiated publisher contacts"), Cell.number(contact[0])),
                            Row.of(Cell.text("Permissions granted"), Cell.number(contact[1])))));
            tables.add(new Table(List.of("New Archiving", ""),
                    List.of(Row.of(Cell.text("New titles successfully archived"),
                            Cell.number(newArchiving.getOrDefault(agencyId, 0L))))));

            long gathered = instanceActivity.gathered.getOrDefault(agencyId, 0L);
            long gatheredFirst = instanceActivity.gatheredFirst.getOrDefault(agencyId, 0L);
            long gatheredAndArchived = instanceActivity.gatheredAndArchived.getOrDefault(agencyId, 0L);
            long gatheredAndArchivedFirst = instanceActivity.gatheredAndArchivedFirst.getOrDefault(agencyId, 0L);
            long gatheredNotArchived = instanceActivity.gatheredNotArchived.getOrDefault(agencyId, 0L);
            long gatheredNotArchivedFirst = instanceActivity.gatheredNotArchivedFirst.getOrDefault(agencyId, 0L);
            tables.add(new Table(List.of("Re-archiving", ""),
                    List.of(Row.of(Cell.text("Instances re-gathered"), Cell.number(gathered - gatheredFirst)),
                            Row.of(Cell.text("Instances successfully re-gathered and published"),
                                    Cell.number(gatheredAndArchived - gatheredAndArchivedFirst)),
                            Row.of(Cell.text("Instances gathered but not published"),
                                    Cell.number(gatheredNotArchived - gatheredNotArchivedFirst)))));

            tables.add(new Table(List.of("Processing", ""),
                    List.of(Row.of(Cell.text("Serial instances processed"),
                                    Cell.number(instanceActivity.archivedSerial.getOrDefault(agencyId, 0L))),
                            Row.of(Cell.text("All instances processed"),
                                    Cell.number(instanceActivity.archived.getOrDefault(agencyId, 0L))))));

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

    @SuppressWarnings("unchecked")
    private InstanceActivity instanceActivity(Instant start, Instant end) {
        InstanceActivity activity = new InstanceActivity();
        for (Object[] r : (List<Object[]>) em.createNativeQuery("""
                select t.agency_id,
                  count(distinct i.instance_id),
                  count(distinct case when f.name = 'Serial' then i.instance_id end)
                from state_history sh
                join instance i on i.instance_id = sh.instance_id
                join title t on t.title_id = i.title_id
                left join format f on f.format_id = t.format_id
                where sh.state_id = :archived
                  and sh.start_date >= :start
                  and sh.start_date < :end
                group by t.agency_id
                """)
                .setParameter("archived", State.ARCHIVED.id())
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList()) {
            Long agencyId = ((Number) r[0]).longValue();
            activity.archived.put(agencyId, number(r[1]));
            activity.archivedSerial.put(agencyId, number(r[2]));
        }

        for (Object[] r : (List<Object[]>) em.createNativeQuery("""
                select gathered_instances.agency_id,
                  count(*),
                  sum(gathered_instances.has_previous),
                  sum(gathered_instances.has_archived),
                  sum(case when gathered_instances.has_archived = 1 then gathered_instances.has_previous else 0 end),
                  sum(case when gathered_instances.has_archived = 0 then 1 else 0 end),
                  sum(case when gathered_instances.has_archived = 0 then gathered_instances.has_previous else 0 end)
                from (
                  select t.agency_id, i.instance_id,
                    case when exists (
                      select 1 from instance previous_instance
                      where previous_instance.title_id = i.title_id
                        and previous_instance.instance_date < i.instance_date
                    ) then 1 else 0 end as has_previous,
                    case when archived_state.instance_id is null then 0 else 1 end as has_archived
                  from (
                    select distinct instance_id
                    from state_history
                    where state_id = :gathered
                      and start_date >= :start
                      and start_date < :end
                  ) gathered_state
                  join instance i on i.instance_id = gathered_state.instance_id
                  join title t on t.title_id = i.title_id
                  left join (
                    select distinct instance_id
                    from state_history
                    where state_id = :archived
                      and start_date >= :start
                      and start_date < :end
                  ) archived_state on archived_state.instance_id = i.instance_id
                ) gathered_instances
                group by gathered_instances.agency_id
                """)
                .setParameter("gathered", State.GATHERED.id())
                .setParameter("archived", State.ARCHIVED.id())
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList()) {
            Long agencyId = ((Number) r[0]).longValue();
            long gathered = number(r[1]);
            long reGathered = number(r[2]);
            long gatheredAndArchived = number(r[3]);
            long reGatheredAndArchived = number(r[4]);
            long gatheredNotArchived = number(r[5]);
            long reGatheredNotArchived = number(r[6]);
            activity.gathered.put(agencyId, gathered);
            activity.gatheredFirst.put(agencyId, gathered - reGathered);
            activity.gatheredAndArchived.put(agencyId, gatheredAndArchived);
            activity.gatheredAndArchivedFirst.put(agencyId, gatheredAndArchived - reGatheredAndArchived);
            activity.gatheredNotArchived.put(agencyId, gatheredNotArchived);
            activity.gatheredNotArchivedFirst.put(agencyId, gatheredNotArchived - reGatheredNotArchived);
        }
        return activity;
    }

    private static long number(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }

    private static class InstanceActivity {
        private final Map<Long, Long> gathered = new LinkedHashMap<>();
        private final Map<Long, Long> gatheredFirst = new LinkedHashMap<>();
        private final Map<Long, Long> archived = new LinkedHashMap<>();
        private final Map<Long, Long> archivedSerial = new LinkedHashMap<>();
        private final Map<Long, Long> gatheredAndArchived = new LinkedHashMap<>();
        private final Map<Long, Long> gatheredAndArchivedFirst = new LinkedHashMap<>();
        private final Map<Long, Long> gatheredNotArchived = new LinkedHashMap<>();
        private final Map<Long, Long> gatheredNotArchivedFirst = new LinkedHashMap<>();

        private boolean hasData(Long agencyId) {
            return gathered.containsKey(agencyId) || archived.containsKey(agencyId);
        }
    }

    private static Row selectionRow(String label, long[] c, boolean total) {
        long rowTotal = c[SELECTED] + c[REJECTED] + c[MONITORED];
        return new Row(List.of(Cell.text(label), Cell.number(c[SELECTED]), Cell.number(c[REJECTED]),
                Cell.number(c[MONITORED]), Cell.number(rowTotal)), total);
    }
}
