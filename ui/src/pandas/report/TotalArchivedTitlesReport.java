package pandas.report;

import org.springframework.stereotype.Component;
import pandas.agency.AgencyRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Report 1: a numbers-only summary of the archived holdings per agency (titles, instances, files and
 * bytes). When run without a period it also adds the pre-PANDAS legacy baseline to the totals.
 */
@Component
public class TotalArchivedTitlesReport implements ReportDefinition {
    private final AgencyRepository agencyRepository;
    private final ReportConfig config;

    public TotalArchivedTitlesReport(AgencyRepository agencyRepository, ReportConfig config) {
        this.agencyRepository = agencyRepository;
        this.config = config;
    }

    @Override
    public String slug() {
        return "total-archived-titles";
    }

    @Override
    public String name() {
        return "Total Archived Titles Statistics";
    }

    @Override
    public String description() {
        return "Archived titles, instances, files and bytes per agency.";
    }

    @Override
    public boolean hasPeriod() {
        return true;
    }

    @Override
    public ReportView generate(ReportParams params) {
        Instant start = params.periodStart() == null ? null : params.startOrEpoch();
        Instant end = params.periodEnd() == null ? null : params.endOrNow();

        var stats = new ArrayList<>(agencyRepository.archivingStats(start, end));
        stats.removeIf(s -> config.isExcluded(s.getId()));
        stats.sort(Comparator.comparing(s -> s.getName() == null ? "" : s.getName()));

        List<Row> rows = new ArrayList<>();
        long totalTitles = 0, totalInstances = 0, totalFiles = 0, totalBytes = 0;
        for (var s : stats) {
            long titles = nz(s.getTitles()), instances = nz(s.getInstances()), files = nz(s.getFiles()), bytes = nz(s.getSize());
            totalTitles += titles;
            totalInstances += instances;
            totalFiles += files;
            totalBytes += bytes;
            rows.add(Row.of(Cell.text(s.getName()), Cell.number(titles), Cell.number(instances),
                    Cell.number(files), Cell.bytes(bytes)));
        }

        if (params.hasPeriod()) {
            rows.add(Row.total(Cell.text("Total"), Cell.number(totalTitles), Cell.number(totalInstances),
                    Cell.number(totalFiles), Cell.bytes(totalBytes)));
        } else {
            // Whole-of-archive view: show the live subtotal, the pre-PANDAS legacy baseline, and their sum.
            rows.add(Row.total(Cell.text("Subtotal"), Cell.number(totalTitles), Cell.number(totalInstances),
                    Cell.number(totalFiles), Cell.bytes(totalBytes)));
            rows.add(Row.total(Cell.text("Legacy data (pre-PANDAS)"), Cell.EMPTY, Cell.EMPTY,
                    Cell.number(config.getLegacyFiles()), Cell.bytes(config.getLegacyBytes())));
            rows.add(Row.total(Cell.text("Total"), Cell.number(totalTitles), Cell.number(totalInstances),
                    Cell.number(totalFiles + config.getLegacyFiles()), Cell.bytes(totalBytes + config.getLegacyBytes())));
        }

        Table table = new Table(List.of("Agency", "Titles", "Instances", "Total files", "Total bytes"), rows);
        return new ReportView(name(), params.periodSubheading(), List.of(new Section(null, table)));
    }

    private static long nz(Long value) {
        return value == null ? 0 : value;
    }
}
