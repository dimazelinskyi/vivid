package io.github.dimazelinskyi.vivid.table;

import io.github.dimazelinskyi.vivid.render.BoxStyle;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable table with a header, rows, and box-drawn borders.
 *
 * <p>Column widths are computed at render time from the content and the available width,
 * so the same table adapts to narrow and wide terminals.
 *
 * <pre>{@code
 * Table table = Table.builder()
 *         .title("Dependencies")
 *         .columns("Name", "Version")
 *         .column(Column.of("Size").right())
 *         .row("vivid", "0.1.0", "48 KB")
 *         .row("junit-jupiter", "5.13.4", "[dim]test only[/]")
 *         .box(BoxStyle.ROUNDED)
 *         .build();
 *
 * console.println(table);
 * }</pre>
 *
 * @param title       optional title shown above the table, interpreted as markup; {@code null} for none
 * @param columns     the column definitions
 * @param rows        the data rows; each has at most {@code columns.size()} cells
 * @param box         the border characters
 * @param borderStyle style applied to the border
 * @param headerStyle style applied to all header cells
 * @param showHeader  whether to draw the header row
 * @param expand      whether to stretch the table to the full available width
 */
public record Table(
        String title,
        List<Column> columns,
        List<Row> rows,
        BoxStyle box,
        Style borderStyle,
        Style headerStyle,
        boolean showHeader,
        boolean expand) implements Renderable {

    public Table {
        columns = List.copyOf(columns);
        rows = List.copyOf(rows);
        box = box == null ? BoxStyle.ROUNDED : box;
        borderStyle = borderStyle == null ? Style.NONE : borderStyle;
        headerStyle = headerStyle == null ? Style.NONE : headerStyle;
        for (Row row : rows) {
            if (row.size() > columns.size()) {
                throw new IllegalArgumentException(
                        "Row has " + row.size() + " cells but the table has only " + columns.size() + " columns");
            }
        }
    }

    /**
     * Starts building a new table.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> render(Renderable.Context context) {
        throw new UnsupportedOperationException("Table rendering is not implemented yet");
    }

    /**
     * A fluent builder for {@link Table}. Mutable and not thread-safe.
     */
    public static final class Builder {

        private String title;
        private final List<Column> columns = new ArrayList<>();
        private final List<Row> rows = new ArrayList<>();
        private BoxStyle box = BoxStyle.ROUNDED;
        private Style borderStyle = Style.NONE;
        private Style headerStyle = Style.builder().bold().build();
        private boolean showHeader = true;
        private boolean expand;

        private Builder() {
        }

        /**
         * Sets the title shown above the table.
         *
         * @param title the title, interpreted as markup
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Adds a column with default settings.
         *
         * @param header the header text
         * @return this builder
         */
        public Builder column(String header) {
            return column(Column.of(header));
        }

        /**
         * Adds a fully configured column.
         *
         * @param column the column
         * @return this builder
         */
        public Builder column(Column column) {
            columns.add(Objects.requireNonNull(column, "column"));
            return this;
        }

        /**
         * Adds several columns with default settings.
         *
         * @param headers the header texts
         * @return this builder
         */
        public Builder columns(String... headers) {
            for (String header : headers) {
                column(header);
            }
            return this;
        }

        /**
         * Adds a row of cells.
         *
         * @param cells the cell values; strings are interpreted as markup
         * @return this builder
         */
        public Builder row(Object... cells) {
            return row(Row.of(cells));
        }

        /**
         * Adds a pre-built row.
         *
         * @param row the row
         * @return this builder
         */
        public Builder row(Row row) {
            rows.add(Objects.requireNonNull(row, "row"));
            return this;
        }

        /**
         * Sets the border characters.
         *
         * @param box the box style
         * @return this builder
         */
        public Builder box(BoxStyle box) {
            this.box = Objects.requireNonNull(box, "box");
            return this;
        }

        /**
         * Sets the style of the border.
         *
         * @param borderStyle the border style
         * @return this builder
         */
        public Builder borderStyle(Style borderStyle) {
            this.borderStyle = borderStyle;
            return this;
        }

        /**
         * Sets the style applied to all header cells. Defaults to bold.
         *
         * @param headerStyle the header style
         * @return this builder
         */
        public Builder headerStyle(Style headerStyle) {
            this.headerStyle = headerStyle;
            return this;
        }

        /**
         * Sets whether the header row is drawn. Defaults to {@code true}.
         *
         * @param showHeader {@code false} to hide the header
         * @return this builder
         */
        public Builder showHeader(boolean showHeader) {
            this.showHeader = showHeader;
            return this;
        }

        /**
         * Sets whether the table stretches to the full available width. Defaults to {@code false}.
         *
         * @param expand {@code true} to fill the width
         * @return this builder
         */
        public Builder expand(boolean expand) {
            this.expand = expand;
            return this;
        }

        /**
         * Creates the immutable table.
         *
         * @return the table
         * @throws IllegalArgumentException if a row has more cells than there are columns
         */
        public Table build() {
            return new Table(title, columns, rows, box, borderStyle, headerStyle, showHeader, expand);
        }
    }
}
