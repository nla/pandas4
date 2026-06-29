package pandas.report;

import java.util.List;

/**
 * The rendered result of running a report: a title, an optional subheading (typically the period), and
 * an ordered list of sections. This is the single structured model that is rendered both as HTML
 * (ReportView.html) and as CSV (ReportController.writeCsv).
 */
public record ReportView(String title, String subheading, List<Section> sections) {
}
