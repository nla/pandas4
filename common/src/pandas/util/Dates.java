package pandas.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.time.ZoneOffset.UTC;

public class Dates {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);
}
