package io.github.dimazelinskyi.vivid.table;

import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.Objects;

/**
 * The definition of a single {@link Table} column: its header and how its cells are laid out.
 *
 * <pre>{@code
 * Column price = Column.of("Price").right().withStyle(Style.of(Color.GREEN));
 * Column notes = Column.of("Notes").withWidth(10, 40);
 * }</pre>
 *
 * @param header      the header text, interpreted as markup
 * @param justify     horizontal alignment of cells in this column
 * @param style       style applied to every cell in this column
 * @param headerStyle style applied to the header cell, on top of the table's header style
 * @param minWidth    minimum width in cells, or {@link #AUTO}
 * @param maxWidth    maximum width in cells, or {@link #AUTO}
 * @param wrap        whether overlong cell content wraps onto new lines ({@code true}) or is truncated
 */
public record Column(
        String header,
        Justify justify,
        Style style,
        Style headerStyle,
        int minWidth,
        int maxWidth,
        boolean wrap) {

    /** Width value meaning "size to fit the content". */
    public static final int AUTO = 0;

    public Column {
        Objects.requireNonNull(header, "header");
        justify = justify == null ? Justify.LEFT : justify;
        style = style == null ? Style.NONE : style;
        headerStyle = headerStyle == null ? Style.NONE : headerStyle;
        if (minWidth < 0 || maxWidth < 0) {
            throw new IllegalArgumentException("Column widths must not be negative");
        }
        if (maxWidth != AUTO && minWidth > maxWidth) {
            throw new IllegalArgumentException("minWidth " + minWidth + " exceeds maxWidth " + maxWidth);
        }
    }

    /**
     * Creates a left-aligned, auto-sized, wrapping column.
     *
     * @param header the header text
     * @return the column
     */
    public static Column of(String header) {
        return new Column(header, Justify.LEFT, Style.NONE, Style.NONE, AUTO, AUTO, true);
    }

    /**
     * Returns a copy with a different alignment.
     *
     * @param newJustify the alignment
     * @return the new column
     */
    public Column withJustify(Justify newJustify) {
        return new Column(header, newJustify, style, headerStyle, minWidth, maxWidth, wrap);
    }

    /** Returns a left-aligned copy. Shorthand for {@code withJustify(Justify.LEFT)}. */
    public Column left() {
        return withJustify(Justify.LEFT);
    }

    /** Returns a centered copy. Shorthand for {@code withJustify(Justify.CENTER)}. */
    public Column center() {
        return withJustify(Justify.CENTER);
    }

    /** Returns a right-aligned copy, typically for numbers. Shorthand for {@code withJustify(Justify.RIGHT)}. */
    public Column right() {
        return withJustify(Justify.RIGHT);
    }

    /**
     * Returns a copy with a different cell style.
     *
     * @param newStyle the style for every cell in the column
     * @return the new column
     */
    public Column withStyle(Style newStyle) {
        return new Column(header, justify, newStyle, headerStyle, minWidth, maxWidth, wrap);
    }

    /**
     * Returns a copy with a different header style.
     *
     * @param newHeaderStyle the style for the header cell
     * @return the new column
     */
    public Column withHeaderStyle(Style newHeaderStyle) {
        return new Column(header, justify, style, newHeaderStyle, minWidth, maxWidth, wrap);
    }

    /**
     * Returns a copy with a fixed width.
     *
     * @param width the exact width in cells
     * @return the new column
     */
    public Column withWidth(int width) {
        return withWidth(width, width);
    }

    /**
     * Returns a copy with width bounds.
     *
     * @param min the minimum width in cells, or {@link #AUTO}
     * @param max the maximum width in cells, or {@link #AUTO}
     * @return the new column
     */
    public Column withWidth(int min, int max) {
        return new Column(header, justify, style, headerStyle, min, max, wrap);
    }

    /**
     * Returns a copy that truncates overlong content with an ellipsis instead of wrapping it.
     *
     * @return the new column
     */
    public Column noWrap() {
        return new Column(header, justify, style, headerStyle, minWidth, maxWidth, false);
    }
}
