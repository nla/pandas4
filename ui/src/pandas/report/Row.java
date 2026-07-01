package pandas.report;

import java.util.List;

/**
 * A row of cells in a report table. {@code total} rows are styled distinctly (e.g. bold) and represent
 * subtotals/totals rather than data.
 */
public record Row(List<Cell> cells, boolean total) {
    public static Row of(Cell... cells) {
        return new Row(List.of(cells), false);
    }

    public static Row total(Cell... cells) {
        return new Row(List.of(cells), true);
    }
}
