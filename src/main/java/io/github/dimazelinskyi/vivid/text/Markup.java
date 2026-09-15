package io.github.dimazelinskyi.vivid.text;

import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A lightweight, BBCode-inspired markup language for inline styling.
 *
 * <p>Tags contain a {@link io.github.dimazelinskyi.vivid.style.Style#parse(String) style definition} and apply it
 * until the matching closing tag. {@code [/]} closes the most recently opened tag; {@code [/style]} closes
 * the most recent tag with the same style, however it is spelled. Nested tags layer their styles on top of
 * the enclosing ones, and tags still open at the end of the text are closed there.
 *
 * <pre>
 * [bold]Bold[/bold] and [italic red]red italic[/italic red]
 * [white on blue] Highlighted [/] back to normal
 * [#ff8800]Hex colors[/] and [bold]nested [red]styles[/red][/bold]
 * </pre>
 *
 * <p>Brackets that do not contain a valid style, such as {@code [INFO]}, {@code [1, 2]} or
 * {@code [/usr/bin]}, are kept as literal text. A literal {@code [} can always be written as {@code \[};
 * use {@link #escape(String)} for untrusted input.
 */
public final class Markup {

    private Markup() {
    }

    /**
     * Parses markup into styled {@link Text}.
     *
     * @param markup the markup string
     * @return the styled text
     * @throws IllegalArgumentException if the markup is malformed, e.g. a closing tag with no opener
     */
    public static Text parse(String markup) {
        Objects.requireNonNull(markup, "markup");
        StringBuilder plain = new StringBuilder();
        // One slot per opened tag, in opening order, so inner tags are applied after outer ones.
        List<Text.Span> spans = new ArrayList<>();
        List<OpenTag> open = new ArrayList<>();

        int i = 0;
        while (i < markup.length()) {
            char c = markup.charAt(i);
            if (c == '\\' && i + 1 < markup.length() && markup.charAt(i + 1) == '[') {
                plain.append('[');
                i += 2;
                continue;
            }
            int end = c == '[' ? markup.indexOf(']', i + 1) : -1;
            if (end > 0) {
                String tag = markup.substring(i + 1, end);
                boolean handled = tag.startsWith("/")
                        ? close(tag, open, spans, plain.length(), markup)
                        : open(tag, open, spans, plain.length());
                if (handled) {
                    i = end + 1;
                    continue;
                }
            }
            plain.append(c);
            i++;
        }

        for (OpenTag tag : open) {
            tag.finish(spans, plain.length());
        }
        spans.removeIf(Objects::isNull);
        return new Text(plain.toString(), Style.NONE, spans, Justify.LEFT);
    }

    private static boolean open(String tag, List<OpenTag> open, List<Text.Span> spans, int position) {
        Style style = parseStyle(tag);
        if (style == null) {
            return false;
        }
        open.add(new OpenTag(style, position, spans.size()));
        spans.add(null);
        return true;
    }

    private static boolean close(String tag, List<OpenTag> open, List<Text.Span> spans, int position, String markup) {
        String name = tag.substring(1);
        if (name.isBlank()) {
            if (open.isEmpty()) {
                throw new IllegalArgumentException("Closing tag [/] has no open tag to close in markup: " + markup);
            }
            open.remove(open.size() - 1).finish(spans, position);
            return true;
        }
        Style style = parseStyle(name);
        if (style == null) {
            return false;
        }
        for (int i = open.size() - 1; i >= 0; i--) {
            if (open.get(i).style().equals(style)) {
                open.remove(i).finish(spans, position);
                return true;
            }
        }
        throw new IllegalArgumentException("Closing tag [" + tag + "] does not match any open tag in markup: " + markup);
    }

    private static Style parseStyle(String definition) {
        try {
            return Style.parse(definition);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Escapes a string so it is displayed literally when embedded in markup.
     *
     * <p>Always escape user input, file names, and other untrusted content before
     * interpolating it into markup.
     *
     * @param text the string to escape
     * @return the escaped string
     */
    public static String escape(String text) {
        Objects.requireNonNull(text, "text");
        return text.replace("[", "\\[");
    }

    /**
     * Removes all markup tags, returning only the displayable characters.
     *
     * @param markup the markup string
     * @return the plain text
     * @throws IllegalArgumentException if the markup is malformed
     */
    public static String strip(String markup) {
        return parse(markup).plain();
    }

    /** A tag waiting for its closing tag; {@code slot} is its reserved position in the span list. */
    private record OpenTag(Style style, int start, int slot) {

        void finish(List<Text.Span> spans, int end) {
            if (end > start) {
                spans.set(slot, new Text.Span(start, end, style));
            }
        }
    }
}
