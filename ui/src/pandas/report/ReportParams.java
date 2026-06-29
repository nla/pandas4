package pandas.report;

import pandas.util.DateFormats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * The options a report run can be parameterised with. Which of these a given report honours is
 * declared by its capability flags ({@link Report#hasPeriod()} etc); the UI only shows the relevant
 * form fields. All fields are optional; a null agency means "all agencies".
 */
public record ReportParams(Long agencyId, LocalDate periodStart, LocalDate periodEnd,
                           boolean showDetails, Long publisherTypeId, Long restrictionType) {

    /** Period start as an instant, or the epoch when unset (the whole-of-archive lower bound). */
    public Instant startOrEpoch() {
        LocalDate d = periodStart != null ? periodStart : LocalDate.ofEpochDay(0);
        return d.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /** Period end (exclusive, i.e. start of the day after periodEnd), or now when unset. */
    public Instant endOrNow() {
        if (periodEnd == null) return Instant.now();
        return periodEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public boolean hasPeriod() {
        return periodStart != null || periodEnd != null;
    }

    /** Formatted period range for use as a report subheading, or null when no period was given. */
    public String periodSubheading() {
        if (!hasPeriod()) return null;
        String from = periodStart != null ? DateFormats.SHORT_DATE.format(periodStart) : "the beginning";
        String to = periodEnd != null ? DateFormats.SHORT_DATE.format(periodEnd) : "now";
        return from + " to " + to;
    }
}
