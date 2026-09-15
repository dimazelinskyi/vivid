package io.github.dimazelinskyi.vivid.render;

/**
 * The set of characters used to draw borders around {@link io.github.dimazelinskyi.vivid.table.Table tables}
 * and {@link io.github.dimazelinskyi.vivid.panel.Panel panels}.
 *
 * <pre>
 *   ROUNDED   SQUARE    HEAVY     DOUBLE    ASCII
 *   ╭──┬──╮   ┌──┬──┐   ┏━━┳━━┓   ╔══╦══╗   +--+--+
 *   ├──┼──┤   ├──┼──┤   ┣━━╋━━┫   ╠══╬══╣   +--+--+
 *   ╰──┴──╯   └──┴──┘   ┗━━┻━━┛   ╚══╩══╝   +--+--+
 * </pre>
 *
 * <p>{@link #ASCII} is the safe fallback for terminals without Unicode support.
 * {@link #NONE} tells renderers to omit the border entirely.
 */
public enum BoxStyle {

    /** Light lines with rounded corners. The default. */
    ROUNDED("╭╮╰╯─│┬┴┤├┼"),

    /** Light lines with square corners. */
    SQUARE("┌┐└┘─│┬┴┤├┼"),

    /** Heavy (thick) lines. */
    HEAVY("┏┓┗┛━┃┳┻┫┣╋"),

    /** Double lines. */
    DOUBLE("╔╗╚╝═║╦╩╣╠╬"),

    /** Plain ASCII, for terminals that cannot display box-drawing characters. */
    ASCII("++++-|+++++"),

    /** No border at all. */
    NONE("           ");

    private final String chars;

    BoxStyle(String chars) {
        if (chars.length() != 11) {
            throw new IllegalArgumentException("A box style needs exactly 11 characters");
        }
        this.chars = chars;
    }

    /** Returns the top-left corner character. */
    public char topLeft() { return chars.charAt(0); }

    /** Returns the top-right corner character. */
    public char topRight() { return chars.charAt(1); }

    /** Returns the bottom-left corner character. */
    public char bottomLeft() { return chars.charAt(2); }

    /** Returns the bottom-right corner character. */
    public char bottomRight() { return chars.charAt(3); }

    /** Returns the horizontal line character. */
    public char horizontal() { return chars.charAt(4); }

    /** Returns the vertical line character. */
    public char vertical() { return chars.charAt(5); }

    /** Returns the T-junction opening downwards, used where a column divider meets the top edge. */
    public char teeDown() { return chars.charAt(6); }

    /** Returns the T-junction opening upwards, used where a column divider meets the bottom edge. */
    public char teeUp() { return chars.charAt(7); }

    /** Returns the T-junction opening to the left, used where a row divider meets the right edge. */
    public char teeLeft() { return chars.charAt(8); }

    /** Returns the T-junction opening to the right, used where a row divider meets the left edge. */
    public char teeRight() { return chars.charAt(9); }

    /** Returns the four-way junction where row and column dividers cross. */
    public char cross() { return chars.charAt(10); }
}
