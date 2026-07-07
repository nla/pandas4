package pandas.report;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report 3: titles with a gather scheduled within the period, per agency, to support workload planning.
 * Combines each title's primary next gather date with any extra one-off dates.
 */
@Component
public class ScheduledGathersReport implements ReportDefinition {
    private final EntityManager em;
    private final AgencyRepository agencyRepository;

    public ScheduledGathersReport(EntityManager em, AgencyRepository agencyRepository) {
        this.em = em;
        this.agencyRepository = agencyRepository;
    }

    @Override
    public String slug() {
        return "scheduled-gathers";
    }

    @Override
    public String name() {
        return "Scheduled Gathers";
    }

    @Override
    public String description() {
        return "Titles with a gather scheduled in the period, per agency.";
    }

    @Override
    public boolean hasPeriod() {
        return true;
    }

    @Override
    public boolean futurePeriod() {
        return true;
    }

    @Override
    public boolean hasAgency() {
        return true;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    private record GatherRow(long agencyId, Long titleId, Long pi, String name, LocalDate date, String schedule,
                             String method, String owner) {
    }

    @Override
    public ReportView generate(ReportParams params) {
        Map<Long, String> agencies = agencies(params.agencyId());

        // Scheduled gathers are typically in the future, so default the upper bound far ahead rather
        // than to "now" when no period end is given.
        Instant start = params.startOrEpoch();
        Instant end = params.periodEnd() != null ? params.endOrNow()
                : LocalDate.now().plusYears(50).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Each title's primary next gather date, plus any extra one-off dates that differ from it.
        String primary = "select t.agency.id, t.id, t.pi, t.name, tg.nextGatherDate, gs.name, gm.name, o.userid "
                + "from Title t join t.gather tg "
                + "left join tg.schedule gs left join tg.method gm left join t.owner o "
                + "where tg.nextGatherDate >= :start and tg.nextGatherDate < :end";
        String oneoff = "select t.agency.id, t.id, t.pi, t.name, d.date, gs.name, gm.name, o.userid "
                + "from Title t join t.gather tg join tg.oneoffDates d "
                + "left join tg.schedule gs left join tg.method gm left join t.owner o "
                + "where d.date >= :start and d.date < :end "
                + "and (tg.nextGatherDate is null or d.date <> tg.nextGatherDate)";

        Map<Long, List<GatherRow>> byAgency = new LinkedHashMap<>();
        for (String jpql : List.of(primary, oneoff)) {
            List<Object[]> rows = em.createQuery(jpql, Object[].class)
                    .setParameter("start", start).setParameter("end", end).getResultList();
            for (Object[] r : rows) {
                long agencyId = (Long) r[0];
                Instant date = (Instant) r[4];
                byAgency.computeIfAbsent(agencyId, k -> new ArrayList<>()).add(new GatherRow(
                        agencyId, (Long) r[1], (Long) r[2], (String) r[3],
                        date == null ? null : LocalDate.ofInstant(date, ZoneId.systemDefault()),
                        schedule((String) r[5]), (String) r[6], (String) r[7]));
            }
        }
        byAgency.values().forEach(list -> list.sort(Comparator.comparing(
                GatherRow::date, Comparator.nullsLast(Comparator.naturalOrder()))));

        List<Section> sections = new ArrayList<>();
        for (var entry : agencies.entrySet()) {
            List<GatherRow> gathers = byAgency.getOrDefault(entry.getKey(), List.of());
            if (gathers.isEmpty()) continue;
            List<Row> rows = new ArrayList<>();
            for (GatherRow g : gathers) {
                rows.add(Row.of(NewlyArchivedTitlesReport.piLink(g.pi()),
                        NewlyArchivedTitlesReport.titleLink(g.titleId(), g.name()), Cell.date(g.date()),
                        Cell.text(g.schedule() == null ? "unknown" : g.schedule()),
                        Cell.text(g.method() == null ? "unknown" : g.method()),
                        Cell.text(g.owner())));
            }
            sections.add(new Section(entry.getValue() + " (" + gathers.size() + ")",
                    new Table(List.of("PI", "Title", "Gather Date", "Schedule", "Method", "Owner"), rows)));
        }
        return new ReportView(name() + " (details)", params.periodSubheading(), sections);
    }

    private Map<Long, String> agencies(Long only) {
        Map<Long, String> result = new LinkedHashMap<>();
        for (Agency agency : agencyRepository.findAllOrdered()) {
            if (only != null && !only.equals(agency.getId())) continue;
            result.put(agency.getId(), agency.getOrganisation().getName());
        }
        return result;
    }

    private static String schedule(String schedule) {
        return "None".equals(schedule) ? "Once" : schedule;
    }
}
