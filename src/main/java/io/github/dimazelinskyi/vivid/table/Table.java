package io.github.dimazelinskyi.vivid.table;

import io.github.dimazelinskyi.vivid.render.Ansi;
import io.github.dimazelinskyi.vivid.render.BoxStyle;
import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.render.Lines;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Renders the title, header, rows and borders.
     *
     * <p>Each column is as wide as its widest content, within the column's width bounds. If the table
     * does not fit, the widest columns are narrowed first; with {@link #expand()} extra width is shared
     * out. Overlong cells are truncated with an ellipsis. Text cells get the row style, then the column
     * style, beneath their own styling, and the column alignment unless they set one themselves.
     *
     * @param context the available width and color depth
     * @return the rendered lines
     */
    @Override
    public List<String> render(Renderable.Context context) {
        Objects.requireNonNull(context, "context");
        int count = columns.size();
        if (count == 0) {
            return List.of();
        }
        Color.Depth depth = context.colorDepth();
        boolean bordered = box != BoxStyle.NONE;

        List<Renderable> header = new ArrayList<>();
        for (Column column : columns) {
            header.add(cell(Text.markup(column.header()), headerStyle.combine(column.headerStyle()), column.justify()));
        }
        List<List<Renderable>> body = new ArrayList<>();
        for (Row row : rows) {
            List<Renderable> cells = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Column column = columns.get(i);
                Renderable value = i < row.size() ? row.cells().get(i) : Text.of("");
                cells.add(cell(value, row.style().combine(column.style()), column.justify()));
            }
            body.add(cells);
        }

        int[] widths = columnWidths(header, body, context, bordered);
        int tableWidth = Arrays.stream(widths).sum() + overhead(count, bordered);

        List<String> lines = new ArrayList<>();
        if (title != null && !title.isEmpty()) {
            Text heading = Text.markup(title).withJustify(Justify.CENTER);
            for (String line : heading.render(new Renderable.Context(tableWidth, depth))) {
                lines.add(Lines.truncate(line, tableWidth));
            }
        }
        if (bordered) {
            lines.add(rule(box.topLeft(), box.teeDown(), box.topRight(), widths, depth));
        }
        if (showHeader) {
            addRow(lines, header, widths, depth, bordered);
            if (bordered) {
                lines.add(rule(box.teeRight(), box.cross(), box.teeLeft(), widths, depth));
            }
        }
        for (List<Renderable> cells : body) {
            addRow(lines, cells, widths, depth, bordered);
        }
        if (bordered) {
            lines.add(rule(box.bottomLeft(), box.teeUp(), box.bottomRight(), widths, depth));
        }
        return List.copyOf(lines);
    }

    private static Renderable cell(Renderable value, Style style, Justify justify) {
        if (!(value instanceof Text text)) {
            return value;
        }
        Justify effective = text.justify() == Justify.LEFT ? justify : text.justify();
        return text.withStyle(style.combine(text.style())).withJustify(effective);
    }

    /** Border and cell padding around the content: {@code │ a │ b │} or, without a border, {@code  a  b }. */
    private static int overhead(int count, boolean bordered) {
        return bordered ? 3 * count + 1 : 2 * count;
    }

    private int[] columnWidths(List<Renderable> header, List<List<Renderable>> body,
                               Renderable.Context context, boolean bordered) {
        int count = columns.size();
        int[] widths = new int[count];
        for (int i = 0; i < count; i++) {
            Column column = columns.get(i);
            int natural = showHeader ? header.get(i).measure(context) : 0;
            for (List<Renderable> cells : body) {
                natural = Math.max(natural, cells.get(i).measure(context));
            }
            if (column.minWidth() != Column.AUTO) {
                natural = Math.max(natural, column.minWidth());
            }
            if (column.maxWidth() != Column.AUTO) {
                natural = Math.min(natural, column.maxWidth());
            }
            widths[i] = Math.max(1, natural);
        }

        int available = context.maxWidth() - overhead(count, bordered);
        int total = Arrays.stream(widths).sum();
        while (total > available) {
            int widest = -1;
            for (int i = 0; i < count; i++) {
                boolean shrinkable = widths[i] > Math.max(1, columns.get(i).minWidth());
                if (shrinkable && (widest < 0 || widths[i] > widths[widest])) {
                    widest = i;
                }
            }
            if (widest < 0) {
                break;
            }
            widths[widest]--;
            total--;
        }
        while (expand && total < available) {
            boolean grew = false;
            for (int i = 0; i < count && total < available; i++) {
                int max = columns.get(i).maxWidth();
                if (max == Column.AUTO || widths[i] < max) {
                    widths[i]++;
                    total++;
                    grew = true;
                }
            }
            if (!grew) {
                break;
            }
        }
        return widths;
    }

    private void addRow(List<String> lines, List<Renderable> cells, int[] widths, Color.Depth depth, boolean bordered) {
        List<List<String>> rendered = new ArrayList<>();
        int height = 1;
        for (int i = 0; i < cells.size(); i++) {
            List<String> cellLines = cells.get(i).render(new Renderable.Context(widths[i], depth));
            rendered.add(cellLines);
            height = Math.max(height, cellLines.size());
        }
        String divider = bordered ? Ansi.styled(String.valueOf(box.vertical()), borderStyle, depth) : "";
        for (int line = 0; line < height; line++) {
            StringBuilder out = new StringBuilder(divider);
            for (int i = 0; i < cells.size(); i++) {
                List<String> cellLines = rendered.get(i);
                String content = line < cellLines.size() ? cellLines.get(line) : "";
                out.append(' ').append(Lines.fit(content, widths[i])).append(' ').append(divider);
            }
            lines.add(out.toString());
        }
    }

    private String rule(char left, char middle, char right, int[] widths, Color.Depth depth) {
        String horizontal = String.valueOf(box.horizontal());
        StringBuilder out = new StringBuilder().append(left);
        for (int i = 0; i < widths.length; i++) {
            out.append(horizontal.repeat(widths[i] + 2)).append(i < widths.length - 1 ? middle : right);
        }
        return Ansi.styled(out.toString(), borderStyle, depth);
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
