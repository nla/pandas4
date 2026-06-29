package pandas.report;

import java.util.List;

/**
 * A titled section of a report. Most reports emit one section per agency. A section usually holds a
 * single table, but may hold several (e.g. the per-agency dashboard in Statistics By Status).
 */
public record Section(String title, List<Table> tables) {
    public Section(String title, Table table) {
        this(title, List.of(table));
    }
}
