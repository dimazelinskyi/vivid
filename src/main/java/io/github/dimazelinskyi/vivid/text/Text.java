package io.github.dimazelinskyi.vivid.text;

import io.github.dimazelinskyi.vivid.render.Ansi;
import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable styled text: a plain string plus style information.
 *
 * <p>A text has a base {@link #style()} that applies to all of it, plus any number of
 * {@link Span spans} that apply additional styles to character ranges. All operations
 * return new instances.
 *
 * <pre>{@code
 * Text status = Text.of("Status: ")
 *         .append("OK", Style.builder().color(Color.GREEN).bold().build());
 *
 * Text fromMarkup = Text.markup("[bold]Status:[/] [green]OK[/]");
 * }</pre>
 *
 * @param plain   the unstyled characters
 * @param style   the base style applied to the whole text
 * @param spans   additional styles applied to ranges of {@code plain}, in application order
 * @param justify alignment used when the text is narrower than the space it is rendered into
 */
public record Text(String plain, Style style, List<Span> spans, Justify justify) implements Renderable {

    public Text {
        Objects.requireNonNull(plain, "plain");
        style = style == null ? Style.NONE : style;
        spans = spans == null ? List.of() : List.copyOf(spans);
        justify = justify == null ? Justify.LEFT : justify;
        for (Span span : spans) {
            if (span.end() > plain.length()) {
                throw new IllegalArgumentException("Span " + span + " exceeds text length " + plain.length());
            }
        }
    }

    /**
     * Creates unstyled text. The string is taken literally; markup is <em>not</em> interpreted.
     *
     * @param plain the text
     * @return the text
     */
    public static Text of(String plain) {
        return new Text(plain, Style.NONE, List.of(), Justify.LEFT);
    }

    /**
     * Creates text with a single style applied to all of it.
     *
     * @param plain the text
     * @param style the style
     * @return the text
     */
    public static Text styled(String plain, Style style) {
        return new Text(plain, style, List.of(), Justify.LEFT);
    }

    /**
     * Creates text from {@link Markup} such as {@code "[bold red]Error:[/] file not found"}.
     *
     * @param markup the markup string
     * @return the parsed text
     */
    public static Text markup(String markup) {
        return Markup.parse(markup);
    }

    /**
     * Returns a new text with an unstyled string appended.
     *
     * @param more the string to append
     * @return the new text
     */
    public Text append(String more) {
        return append(Text.of(more));
    }

    /**
     * Returns a new text with a styled string appended.
     *
     * @param more       the string to append
     * @param moreStyle  the style for the appended part
     * @return the new text
     */
    public Text append(String more, Style moreStyle) {
        return append(Text.styled(more, moreStyle));
    }

    /**
     * Returns a new text with another text appended. Each part keeps exactly its own styling:
     * this text's base style is narrowed to a span over the original characters, so it does not
     * leak onto the appended part, and the result has a plain base style.
     *
     * @param other the text to append
     * @return the new text
     */
    public Text append(Text other) {
        Objects.requireNonNull(other, "other");
        int offset = plain.length();
        List<Span> merged = new ArrayList<>();
        if (!style.isPlain() && offset > 0) {
            merged.add(new Span(0, offset, style));
        }
        merged.addAll(spans);
        if (!other.style.isPlain()) {
            merged.add(new Span(offset, offset + other.plain.length(), other.style));
        }
        for (Span span : other.spans) {
            merged.add(span.shift(offset));
        }
        return new Text(plain + other.plain, Style.NONE, merged, justify);
    }

    /**
     * Returns a new text with a style applied to the character range {@code [start, end)}.
     *
     * @param rangeStyle the style to apply
     * @param start      the first character, inclusive
     * @param end        the last character, exclusive
     * @return the new text
     */
    public Text stylize(Style rangeStyle, int start, int end) {
        List<Span> merged = new ArrayList<>(spans);
        merged.add(new Span(start, end, rangeStyle));
        return new Text(plain, style, merged, justify);
    }

    /**
     * Returns a copy of this text with a different base style.
     *
     * @param newStyle the base style
     * @return the new text
     */
    public Text withStyle(Style newStyle) {
        return new Text(plain, newStyle, spans, justify);
    }

    /**
     * Returns a copy of this text with a different alignment.
     *
     * @param newJustify the alignment
     * @return the new text
     */
    public Text withJustify(Justify newJustify) {
        return new Text(plain, style, spans, newJustify);
    }

    /**
     * Returns the number of characters in {@link #plain()}.
     *
     * <p>This is the length in UTF-16 code units, not the number of terminal cells the text
     * occupies (which differs for wide characters such as CJK and emoji).
     *
     * @return the character count
     */
    public int length() {
        return plain.length();
    }

    /**
     * Renders this text as one line per line of {@link #plain()}, split on {@code \n} or
     * {@code \r\n} (the separators themselves are not rendered).
     *
     * <p>Each line is self-contained: styles are switched on with ANSI escape sequences and reset
     * before the line ends. Lines shorter than {@code context.maxWidth()} are aligned by
     * {@link #justify()} using leading spaces only; longer lines are not wrapped yet.
     *
     * @param context the available width and color depth
     * @return the rendered lines
     */
    @Override
    public List<String> render(Renderable.Context context) {
        Objects.requireNonNull(context, "context");
        List<String> lines = new ArrayList<>();
        int lineStart = 0;
        while (true) {
            int newline = plain.indexOf('\n', lineStart);
            int lineEnd = newline < 0 ? plain.length() : newline;
            if (newline > lineStart && plain.charAt(newline - 1) == '\r') {
                lineEnd--;
            }
            lines.add(renderLine(lineStart, lineEnd, context));
            if (newline < 0) {
                return List.copyOf(lines);
            }
            lineStart = newline + 1;
        }
    }

    /**
     * Returns the length of the longest line, ignoring justification.
     *
     * @param context unused; text has the same natural width everywhere
     * @return the natural width in cells
     */
    @Override
    public int measure(Renderable.Context context) {
        return plain.lines().mapToInt(String::length).max().orElse(0);
    }

    private String renderLine(int start, int end, Renderable.Context context) {
        StringBuilder line = new StringBuilder();
        int free = context.maxWidth() - (end - start);
        if (free > 0 && justify != Justify.LEFT) {
            line.append(" ".repeat(justify == Justify.RIGHT ? free : free / 2));
        }

        String openSgr = "";
        for (int i = start; i < end; i++) {
            String sgr = Ansi.sgr(styleAt(i), context.colorDepth());
            if (!sgr.equals(openSgr)) {
                if (!openSgr.isEmpty()) {
                    line.append(Ansi.RESET);
                }
                line.append(sgr);
                openSgr = sgr;
            }
            line.append(plain.charAt(i));
        }
        if (!openSgr.isEmpty()) {
            line.append(Ansi.RESET);
        }
        return line.toString();
    }

    private Style styleAt(int index) {
        Style effective = style;
        for (Span span : spans) {
            if (span.start() <= index && index < span.end()) {
                effective = effective.combine(span.style());
            }
        }
        return effective;
    }

    /**
     * A style applied to the character range {@code [start, end)} of a {@link Text}.
     *
     * @param start the first character, inclusive
     * @param end   the last character, exclusive
     * @param style the style to apply
     */
    public record Span(int start, int end, Style style) {

        public Span {
            if (start < 0 || end < start) {
                throw new IllegalArgumentException("Invalid span range [" + start + ", " + end + ")");
            }
            Objects.requireNonNull(style, "style");
        }

        /**
         * Returns this span moved right by {@code offset} characters.
         *
         * @param offset the number of characters to shift by
         * @return the shifted span
         */
        public Span shift(int offset) {
            return new Span(start + offset, end + offset, style);
        }
    }
}
