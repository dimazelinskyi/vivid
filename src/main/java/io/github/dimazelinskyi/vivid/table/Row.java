package io.github.dimazelinskyi.vivid.table;

import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.Arrays;
import java.util.List;

/**
 * A single row of {@link Table} cells.
 *
 * <p>Cells are {@link Renderable}s, so a cell can hold anything Vivid can draw — including
 * a nested table or a progress bar. Plain objects are adapted with {@link Renderable#from(Object)}.
 *
 * <pre>{@code
 * Row row = Row.of("vivid", "0.1.0", Text.markup("[green]✔[/]"));
 * Row highlighted = row.withStyle(Style.builder().bold().build());
 * }</pre>
 *
 * @param cells the cells, in column order; may be fewer than the table's columns
 * @param style style applied to the whole row, beneath each column's own style
 */
public record Row(List<Renderable> cells, Style style) {

    public Row {
        cells = List.copyOf(cells);
        style = style == null ? Style.NONE : style;
    }

    /**
     * Creates a row from arbitrary values.
     *
     * @param cells the cell values; strings are interpreted as markup
     * @return the row
     */
    public static Row of(Object... cells) {
        return new Row(Arrays.stream(cells).map(Renderable::from).toList(), Style.NONE);
    }

    /**
     * Returns a copy of this row with a different style.
     *
     * @param newStyle the row style
     * @return the new row
     */
    public Row withStyle(Style newStyle) {
        return new Row(cells, newStyle);
    }

    /**
     * Returns the number of cells in this row.
     *
     * @return the cell count
     */
    public int size() {
        return cells.size();
    }
}
