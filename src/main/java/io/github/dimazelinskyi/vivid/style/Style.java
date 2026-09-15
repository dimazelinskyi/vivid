package io.github.dimazelinskyi.vivid.style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable visual style: an optional foreground color, an optional background color
 * and a set of text {@link Attribute attributes}.
 *
 * <p>A {@code null} color means "not set": the terminal default, or whatever the underlying
 * style provides when styles are layered with {@link #combine(Style)}.
 *
 * <pre>{@code
 * Style warning = Style.builder().color(Color.YELLOW).bold().build();
 * Style alert   = Style.parse("bold white on red");
 * Style loud    = warning.combine(alert);
 * }</pre>
 *
 * @param foreground text color, or {@code null} if unset
 * @param background background color, or {@code null} if unset
 * @param attributes text attributes; defensively copied into an unmodifiable set
 */
public record Style(Color foreground, Color background, Set<Attribute> attributes) {

    /** The empty style: no colors and no attributes. */
    public static final Style NONE = new Style(null, null, Set.of());

    public Style {
        attributes = attributes == null || attributes.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(copyOf(attributes));
    }

    /**
     * Starts building a new style.
     *
     * @return a fresh builder
     */
    public static StyleBuilder builder() {
        return new StyleBuilder();
    }

    /**
     * Returns a style with only a foreground color.
     *
     * @param foreground the text color
     * @return the style
     */
    public static Style of(Color foreground) {
        return new Style(foreground, null, Set.of());
    }

    /**
     * Parses a style definition such as {@code "bold red"}, {@code "italic #ff8800 on black"}
     * or {@code "underline on rgb(30,30,30)"}.
     *
     * <p>Words are case-insensitive. Attributes are named like {@link Attribute} constants
     * ({@code bold}, {@code strikethrough}, ...); any other word is a {@link Color#parse(String) color}.
     *
     * @param definition space-separated attributes and colors; a background is introduced by {@code on}
     * @return the parsed style
     * @throws IllegalArgumentException if the definition is not valid
     */
    public static Style parse(String definition) {
        Objects.requireNonNull(definition, "definition");
        List<String> words = words(definition);
        if (words.isEmpty()) {
            throw new IllegalArgumentException("Style definition is empty");
        }
        Color foreground = null;
        Color background = null;
        EnumSet<Attribute> attributes = EnumSet.noneOf(Attribute.class);
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (word.equalsIgnoreCase("on")) {
                if (i + 1 == words.size()) {
                    throw new IllegalArgumentException("Expected a color after 'on' in style: " + definition);
                }
                if (background != null) {
                    throw new IllegalArgumentException("More than one background color in style: " + definition);
                }
                background = color(words.get(++i), definition);
                continue;
            }
            Attribute attribute = attribute(word);
            if (attribute != null) {
                attributes.add(attribute);
                continue;
            }
            if (foreground != null) {
                throw new IllegalArgumentException("More than one foreground color in style: " + definition);
            }
            foreground = color(word, definition);
        }
        return new Style(foreground, background, attributes);
    }

    /** Splits on whitespace, except inside parentheses so that {@code rgb(1, 2, 3)} stays one word. */
    private static List<String> words(String definition) {
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        int depth = 0;
        for (char c : definition.toCharArray()) {
            if (Character.isWhitespace(c) && depth == 0) {
                if (word.length() > 0) {
                    words.add(word.toString());
                    word.setLength(0);
                }
                continue;
            }
            if (c == '(') {
                depth++;
            } else if (c == ')' && depth > 0) {
                depth--;
            }
            word.append(c);
        }
        if (word.length() > 0) {
            words.add(word.toString());
        }
        return words;
    }

    private static Attribute attribute(String word) {
        for (Attribute attribute : Attribute.values()) {
            if (attribute.name().equalsIgnoreCase(word)) {
                return attribute;
            }
        }
        return null;
    }

    private static Color color(String word, String definition) {
        try {
            return Color.parse(word);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown style word '" + word + "' in style: " + definition, e);
        }
    }

    /**
     * Returns a copy of this style with a different foreground color.
     *
     * @param color the new foreground, or {@code null} to unset it
     * @return the new style
     */
    public Style withForeground(Color color) {
        return new Style(color, background, attributes);
    }

    /**
     * Returns a copy of this style with a different background color.
     *
     * @param color the new background, or {@code null} to unset it
     * @return the new style
     */
    public Style withBackground(Color color) {
        return new Style(foreground, color, attributes);
    }

    /**
     * Returns a copy of this style with the given attributes added.
     *
     * @param added the attributes to add
     * @return the new style
     */
    public Style with(Attribute... added) {
        EnumSet<Attribute> merged = copyOf(attributes);
        merged.addAll(List.of(added));
        return new Style(foreground, background, merged);
    }

    /**
     * Tells whether this style enables the given attribute.
     *
     * @param attribute the attribute to check
     * @return {@code true} if it is enabled
     */
    public boolean has(Attribute attribute) {
        return attributes.contains(attribute);
    }

    /**
     * Tells whether this style has no effect at all.
     *
     * @return {@code true} if no colors and no attributes are set
     */
    public boolean isPlain() {
        return foreground == null && background == null && attributes.isEmpty();
    }

    /**
     * Layers {@code other} on top of this style. Colors set in {@code other} win;
     * attributes from both are kept.
     *
     * @param other the style to apply on top
     * @return the combined style
     */
    public Style combine(Style other) {
        Objects.requireNonNull(other, "other");
        EnumSet<Attribute> merged = copyOf(attributes);
        merged.addAll(other.attributes);
        return new Style(
                other.foreground != null ? other.foreground : foreground,
                other.background != null ? other.background : background,
                merged);
    }

    private static EnumSet<Attribute> copyOf(Collection<Attribute> attributes) {
        EnumSet<Attribute> copy = EnumSet.noneOf(Attribute.class);
        copy.addAll(attributes);
        return copy;
    }
}
