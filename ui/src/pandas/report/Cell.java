package pandas.report;

import org.apache.commons.io.FileUtils;
import pandas.util.DateFormats;

import java.time.LocalDate;

/**
 * A single typed value in a report table. The type drives both HTML formatting (numbers grouped with
 * commas, bytes human-readable, links rendered as anchors) and the raw, analysis-friendly CSV value
 * (plain integers, ISO dates). One immutable value object renders to both formats. {@code cssClass}
 * is added to the rendered cell (e.g. {@code "number"} for right alignment, {@code "date"} to prevent
 * wrapping); it may be null.
 */
public record Cell(String text, String href, String csv, String cssClass) {
    public static final Cell EMPTY = new Cell("", null, "", null);

    public static Cell text(String value) {
        String v = value == null ? "" : value;
        return new Cell(v, null, v, null);
    }

    public static Cell number(long value) {
        return new Cell(String.format("%,d", value), null, Long.toString(value), "number");
    }

    public static Cell bytes(long value) {
        return new Cell(FileUtils.byteCountToDisplaySize(value), null, Long.toString(value), "number");
    }

    public static Cell date(LocalDate value) {
        if (value == null) return EMPTY;
        // Display in a human-friendly locale-aware form; keep the CSV value as a stable ISO date.
        return new Cell(DateFormats.SHORT_DATE.format(value), null, value.toString(), "date");
    }

    public static Cell link(String href, String text) {
        String t = text == null ? "" : text;
        return new Cell(t, href, t, null);
    }
}
