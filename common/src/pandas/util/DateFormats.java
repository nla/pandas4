package pandas.util;

import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static java.time.ZoneOffset.UTC;

@Component
public class DateFormats {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);
    public static final DateTimeFormatter LONG_DATE_TIME = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DAY_DATE_TIME = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' h:mm a").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DAY_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter TIME_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter TIME_WITH_MILLISECONDS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM y").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("d MMM y").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter SHORT_DATE_TIME = DateTimeFormatter.ofPattern("d MMM y h:mma").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter PANDAS_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
    ;

    public DateTimeFormatter getDayDateTime() {
        return DAY_DATE_TIME;
    }

    public DateTimeFormatter getDayDate() {
        return DAY_DATE;
    }

    public DateTimeFormatter getDate() {
        return DATE;
    }

    public DateTimeFormatter getTime() {
        return TIME;
    }

    public DateTimeFormatter getTimeWithSeconds() {
        return TIME_WITH_SECONDS;
    }

    public DateTimeFormatter getTimeWithMilliseconds() {
        return TIME_WITH_MILLISECONDS;
    }

    public String shortDate(TemporalAccessor date) {
        if (date == null) return null;
        return SHORT_DATE.format(date).replace(".", "");
    }

    public DateTimeFormatter getShortDate() {
        return SHORT_DATE;
    }

    public DateTimeFormatter getShortDateTime() {
        return SHORT_DATE_TIME;
    }

    public DateTimeFormatter getMonthYear() {
        return MONTH_YEAR;
    }
}
