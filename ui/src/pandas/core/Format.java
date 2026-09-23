package pandas.core;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Helpers for formatting numbers and URLs for display.
 */
@Service
public class Format {
    public String comma(long number) {
        return String.format("%,d", number);
    }

    public String bytes(long x) {
        return FileUtils.byteCountToDisplaySize(x);
    }

    public String relativeAge(Instant instant) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate date = instant.atZone(zone).toLocalDate();
        LocalDate today = LocalDate.now(zone);
        long days = Math.max(0, ChronoUnit.DAYS.between(date, today));
        if (days == 0) return "today";
        if (days == 1) return "yesterday";
        if (days < 30) return days + " days ago";

        long months = Math.max(1, ChronoUnit.MONTHS.between(date, today));
        if (months < 12) return months == 1 ? "1 month ago" : months + " months ago";

        long years = Math.max(1, ChronoUnit.YEARS.between(date, today));
        return years == 1 ? "1 year ago" : years + " years ago";
    }

    public String statusClass(Integer status) {
        if (status == null || status < 100) {
            return "status-unknown";
        } else if (status < 300) {
            return "status-2xx";
        } else if (status < 400) {
            return "status-3xx";
        } else if (status < 500) {
            return "status-4xx";
        } else if (status < 600) {
            return "status-5xx";
        } else {
            return "status-unknown";
        }
    }

    /**
     * Returns the site part of a URL. Removing the protocol, www. prefix and path.
     */
    public String site(String url) {
        if (url == null) return null;
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException ignored) {
            return url;
        }
        if (host.startsWith("www.")) host = host.substring(4);
        return host;
    }
}
