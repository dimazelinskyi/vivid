package io.github.dimazelinskyi.vivid.style;

/**
 * A text attribute such as bold or underline, independent of color.
 *
 * <p>Not every terminal supports every attribute; unsupported ones are silently ignored
 * by the terminal. {@link #BOLD}, {@link #DIM}, {@link #UNDERLINE} and {@link #REVERSE}
 * are the most widely supported.
 */
public enum Attribute {

    /** Bold or increased intensity. */
    BOLD(1),
    /** Dim or decreased intensity. */
    DIM(2),
    /** Italic. */
    ITALIC(3),
    /** Single underline. */
    UNDERLINE(4),
    /** Slow blink. Use sparingly. */
    BLINK(5),
    /** Swap foreground and background colors. */
    REVERSE(7),
    /** Invisible text (still selectable and copyable). */
    HIDDEN(8),
    /** Strikethrough. */
    STRIKETHROUGH(9);

    private final int sgrCode;

    Attribute(int sgrCode) {
        this.sgrCode = sgrCode;
    }

    /**
     * Returns the ANSI SGR parameter that enables this attribute, as used in
     * {@code ESC [ <code> m}.
     *
     * @return the SGR code
     */
    public int sgrCode() {
        return sgrCode;
    }
}
