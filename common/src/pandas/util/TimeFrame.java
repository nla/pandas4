package pandas.util;

import java.time.Instant;
import java.util.Objects;

public class TimeFrame {
    private final Instant startDate;
    private final Instant endDate;

    public TimeFrame(Instant startDate, Instant endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Instant startDate() {
        return startDate;
    }

    public Instant endDate() {
        return endDate;
    }

    public String toString() {
        String startMonth = startDate == null ? null : DateFormats.MONTH_YEAR.format(startDate);
        String endMonth = endDate == null ? null : DateFormats.MONTH_YEAR.format(endDate);
        if (Objects.equals(startMonth, endMonth)) return startMonth;
        if (startMonth == null) return "- " + endMonth;
        if (endMonth == null) return startMonth + " -";
        return startMonth + " - " + endMonth;
    }
}
