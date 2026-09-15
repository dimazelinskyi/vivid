package io.github.dimazelinskyi.vivid.text;

import java.util.Objects;

/**
 * A lightweight, BBCode-inspired markup language for inline styling.
 *
 * <p>Tags contain a {@link io.github.dimazelinskyi.vivid.style.Style#parse(String) style definition} and apply it
 * until the matching closing tag. {@code [/]} closes the most recently opened tag.
 *
 * <pre>
 * [bold]Bold[/bold] and [italic red]red italic[/italic red]
 * [white on blue] Highlighted [/] back to normal
 * [#ff8800]Hex colors[/] and [link=https://example.com]links[/link]
 * </pre>
 *
 * <p>A literal {@code [} is written as {@code \[}; use {@link #escape(String)} for untrusted input.
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
        throw new UnsupportedOperationException("Markup parsing is not implemented yet");
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
     */
    public static String strip(String markup) {
        return parse(markup).plain();
    }
}
