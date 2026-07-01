package pandas.report;

import java.util.List;

/**
 * A single table within a report section: column headings and the rows beneath them.
 */
public record Table(List<String> columns, List<Row> rows) {
}
