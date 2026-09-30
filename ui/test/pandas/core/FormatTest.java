package pandas.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatTest {
    private final Format format = new Format();

    @Test
    void ageOrYear() {
        LocalDate today = LocalDate.now();
        assertEquals("today", format.ageOrYear(instant(today)));
        assertEquals("3 days ago", format.ageOrYear(instant(today.minusDays(3))));
        assertEquals("5 months ago", format.ageOrYear(instant(today.minusMonths(5))));
        assertEquals("11 months ago", format.ageOrYear(instant(today.minusMonths(11))));
        LocalDate old = today.minusMonths(12);
        assertEquals(String.valueOf(old.getYear()), format.ageOrYear(instant(old)));
        assertEquals("2000", format.ageOrYear(instant(LocalDate.of(2000, 4, 1))));
    }

    private static Instant instant(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
