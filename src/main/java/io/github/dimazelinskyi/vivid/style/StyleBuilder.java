package io.github.dimazelinskyi.vivid.style;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * A fluent builder for {@link Style}.
 *
 * <p>Reads the way you would describe a style out loud:
 *
 * <pre>{@code
 * Style header = Style.builder()
 *         .color(Color.WHITE)
 *         .on(Color.hex("#1e1e2e"))
 *         .bold()
 *         .build();
 * }</pre>
 *
 * <p>Builders are mutable and not thread-safe; the styles they produce are immutable.
 * A builder may be reused — {@link #build()} snapshots its current state.
 */
public final class StyleBuilder {

    private Color foreground;
    private Color background;
    private final EnumSet<Attribute> attributes = EnumSet.noneOf(Attribute.class);

    /** Creates an empty builder. Prefer {@link Style#builder()}. */
    public StyleBuilder() {
    }

    /**
     * Creates a builder pre-populated from an existing style.
     *
     * @param style the style to start from
     * @return a new builder
     */
    public static StyleBuilder from(Style style) {
        Objects.requireNonNull(style, "style");
        StyleBuilder builder = new StyleBuilder();
        builder.foreground = style.foreground();
        builder.background = style.background();
        builder.attributes.addAll(style.attributes());
        return builder;
    }

    /**
     * Sets the foreground (text) color.
     *
     * @param color the color, or {@code null} to unset it
     * @return this builder
     */
    public StyleBuilder color(Color color) {
        this.foreground = color;
        return this;
    }

    /**
     * Sets the foreground (text) color from a hex string such as {@code "#ff8800"}.
     *
     * @param hex the color in hex notation
     * @return this builder
     */
    public StyleBuilder color(String hex) {
        return color(Color.hex(hex));
    }

    /**
     * Sets the background color.
     *
     * @param color the color, or {@code null} to unset it
     * @return this builder
     */
    public StyleBuilder on(Color color) {
        this.background = color;
        return this;
    }

    /**
     * Sets the background color from a hex string such as {@code "#1e1e2e"}.
     *
     * @param hex the color in hex notation
     * @return this builder
     */
    public StyleBuilder on(String hex) {
        return on(Color.hex(hex));
    }

    /**
     * Enables the given attributes.
     *
     * @param added the attributes to enable
     * @return this builder
     */
    public StyleBuilder attribute(Attribute... added) {
        attributes.addAll(List.of(added));
        return this;
    }

    /** Enables {@link Attribute#BOLD}. */
    public StyleBuilder bold() { return attribute(Attribute.BOLD); }

    /** Enables {@link Attribute#DIM}. */
    public StyleBuilder dim() { return attribute(Attribute.DIM); }

    /** Enables {@link Attribute#ITALIC}. */
    public StyleBuilder italic() { return attribute(Attribute.ITALIC); }

    /** Enables {@link Attribute#UNDERLINE}. */
    public StyleBuilder underline() { return attribute(Attribute.UNDERLINE); }

    /** Enables {@link Attribute#BLINK}. */
    public StyleBuilder blink() { return attribute(Attribute.BLINK); }

    /** Enables {@link Attribute#REVERSE}. */
    public StyleBuilder reverse() { return attribute(Attribute.REVERSE); }

    /** Enables {@link Attribute#HIDDEN}. */
    public StyleBuilder hidden() { return attribute(Attribute.HIDDEN); }

    /** Enables {@link Attribute#STRIKETHROUGH}. */
    public StyleBuilder strikethrough() { return attribute(Attribute.STRIKETHROUGH); }

    /**
     * Creates an immutable {@link Style} from the current state of this builder.
     *
     * @return the style
     */
    public Style build() {
        return new Style(foreground, background, attributes);
    }
}
