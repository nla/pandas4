package pandas.report;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import pandas.collection.Status;
import pandas.gather.State;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report 7: legal-deposit titles counted by format and milestone (selected / permission requested /
 * permission granted via status history; archived via instance state), per agency.
 *
 * <p>Uses JPQL rather than native SQL so that the boolean {@code legal_deposit} filter and the status
 * and state enums are emitted correctly for each database dialect (and against the H2 test schema).
 */
@Component
public class LegalDepositReport implements ReportDefinition {
    private static final List<String> FORMATS = List.of("Serial", "Mono", "Integrating");
    private static final int SELECTED = 0, PERMISSION_REQUESTED = 1, PERMISSION_GRANTED = 2, ARCHIVED = 3;

    private final EntityManager em;
    private final ReportSupport support;

    public LegalDepositReport(EntityManager em, ReportSupport support) {
        this.em = em;
        this.support = support;
    }

    @Override
    public String slug() {
        return "legal-deposit";
    }

    @Override
    public String name() {
        return "Legal Deposit";
    }

    @Override
    public String description() {
        return "Legal-deposit titles by format and selection/permission/archiving milestone, per agency.";
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

        // agency -> format -> [selected, permReq, permGranted, archived]
        Map<Long, Map<String, long[]>> counts = new LinkedHashMap<>();

        List<Object[]> statusRows = em.createQuery("""
                select t.agency.id, f.name, sh.status, count(distinct t.id)
                from StatusHistory sh join sh.title t left join t.format f
                where t.legalDeposit = true
                  and sh.startDate >= :start and sh.startDate < :end
                  and sh.status in :statuses
                group by t.agency.id, f.name, sh.status
                """, Object[].class)
                .setParameter("start", params.startOrEpoch())
                .setParameter("end", params.endOrNow())
                .setParameter("statuses", List.of(Status.SELECTED, Status.PERMISSION_REQUESTED, Status.PERMISSION_GRANTED))
                .getResultList();
        for (Object[] r : statusRows) {
            int index = switch ((Status) r[2]) {
                case SELECTED -> SELECTED;
                case PERMISSION_REQUESTED -> PERMISSION_REQUESTED;
                default -> PERMISSION_GRANTED;
            };
            bucket(counts, (Long) r[0], (String) r[1])[index] += (Long) r[3];
        }

        List<Object[]> archivedRows = em.createQuery("""
                select t.agency.id, f.name, count(distinct t.id)
                from Instance i join i.title t left join t.format f
                where t.legalDeposit = true
                  and i.state = :archived
                  and i.date >= :start and i.date < :end
                group by t.agency.id, f.name
                """, Object[].class)
                .setParameter("start", params.startOrEpoch())
                .setParameter("end", params.endOrNow())
                .setParameter("archived", State.ARCHIVED)
                .getResultList();
        for (Object[] r : archivedRows) {
            bucket(counts, (Long) r[0], (String) r[1])[ARCHIVED] += (Long) r[2];
        }

        List<Section> sections = new ArrayList<>();
        for (var entry : agencies.entrySet()) {
            Map<String, long[]> byFormat = counts.get(entry.getKey());
            if (byFormat == null) continue;
            List<Row> rows = new ArrayList<>();
            long[] totals = new long[4];
            for (String format : FORMATS) {
                long[] c = byFormat.getOrDefault(format, new long[4]);
                rows.add(formatRow(format, c, false));
            }
            for (long[] c : byFormat.values()) {
                for (int i = 0; i < 4; i++) totals[i] += c[i];
            }
            rows.add(formatRow("Total", totals, true));
            sections.add(new Section(entry.getValue(), new Table(
                    List.of("Format", "Selected", "Permission Requested", "Permission Granted", "Archived"), rows)));
        }
        return new ReportView(name(), params.periodSubheading(), sections);
    }

    private static Row formatRow(String label, long[] c, boolean total) {
        List<Cell> cells = List.of(Cell.text(label), Cell.number(c[SELECTED]), Cell.number(c[PERMISSION_REQUESTED]),
                Cell.number(c[PERMISSION_GRANTED]), Cell.number(c[ARCHIVED]));
        return new Row(cells, total);
    }

    private static long[] bucket(Map<Long, Map<String, long[]>> counts, long agencyId, String format) {
        return counts.computeIfAbsent(agencyId, k -> new LinkedHashMap<>())
                .computeIfAbsent(format == null ? "Unknown" : format, k -> new long[4]);
    }
}
