package pandas.report;

/**
 * A report that can be generated on demand from a set of {@link ReportParams}. Each report is a Spring
 * {@code @Component}; {@link ReportController} collects them all and exposes them at
 * {@code /reports/{slug}} (HTML) and {@code /reports/{slug}.csv} (CSV).
 *
 * <p>The capability flags declare which parameters the report honours, which drives the fields shown
 * on the parameter form. Adding a new report is simply a matter of adding another {@code @Component}
 * implementing this interface.
 *
 * <p>(Named {@code ReportDefinition} rather than {@code Report} to avoid clashing with the legacy
 * {@code pandas.report.Report} entity that still backs the worktray's report panels.)
 */
public interface ReportDefinition {
    /** Stable URL identifier, e.g. {@code "total-archived-titles"}. */
    String slug();

    /** Human-readable name shown in the index and as the page heading. */
    String name();

    /** One-line description for the report index. */
    default String description() {
        return "";
    }

    default boolean hasPeriod() {
        return false;
    }

    /** Whether the report's period is forward-looking (e.g. scheduled work), which orients the date pickers to future dates. */
    default boolean futurePeriod() {
        return false;
    }

    default boolean hasAgency() {
        return false;
    }

    default boolean hasDetails() {
        return false;
    }

    default boolean hasPublisherType() {
        return false;
    }

    default boolean hasRestrictionType() {
        return false;
    }

    ReportView generate(ReportParams params);
}
