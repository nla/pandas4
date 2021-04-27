package pandas.util;

import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static java.time.ZoneOffset.UTC;

@Component
public class DateFormats {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);
    public static final DateTimeFormatter LONG_DATE_TIME = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter MEDIUM_DATE_TIME = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' h:mm a").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter MEDIUM_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());

    public DateTimeFormatter getDayDateTime() {
        return MEDIUM_DATE_TIME;
    }
}
