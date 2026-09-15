package io.github.dimazelinskyi.vivid.style;

import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * A terminal color.
 *
 * <p>Three kinds of color are supported, matching what terminals can actually display:
 * <ul>
 *   <li>{@link Standard} — the 16 classic ANSI colors, themed by the user's terminal;</li>
 *   <li>{@link Indexed} — the 256-color xterm palette;</li>
 *   <li>{@link Rgb} — 24-bit true color.</li>
 * </ul>
 *
 * <pre>{@code
 * Color a = Color.RED;
 * Color b = Color.hex("#ff8800");
 * Color c = Color.rgb(30, 144, 255);
 * Color d = Color.indexed(208);
 * }</pre>
 *
 * <p>When the terminal supports less than the requested {@link Depth}, renderers will
 * downgrade colors to the nearest available one.
 */
public sealed interface Color permits Color.Standard, Color.Indexed, Color.Rgb {

    /** Standard black. */
    Color BLACK = Standard.BLACK;
    /** Standard red. */
    Color RED = Standard.RED;
    /** Standard green. */
    Color GREEN = Standard.GREEN;
    /** Standard yellow. */
    Color YELLOW = Standard.YELLOW;
    /** Standard blue. */
    Color BLUE = Standard.BLUE;
    /** Standard magenta. */
    Color MAGENTA = Standard.MAGENTA;
    /** Standard cyan. */
    Color CYAN = Standard.CYAN;
    /** Standard white. */
    Color WHITE = Standard.WHITE;

    /**
     * Creates a 24-bit color.
     *
     * @param red   red channel, 0–255
     * @param green green channel, 0–255
     * @param blue  blue channel, 0–255
     * @return the color
     */
    static Rgb rgb(int red, int green, int blue) {
        return new Rgb(red, green, blue);
    }

    /**
     * Parses a 24-bit color from a hex string such as {@code "#ff8800"} or {@code "ff8800"}.
     *
     * @param hex six hex digits, optionally prefixed with {@code #}
     * @return the color
     * @throws IllegalArgumentException if {@code hex} is not a valid hex color
     */
    static Rgb hex(String hex) {
        Objects.requireNonNull(hex, "hex");
        String digits = hex.startsWith("#") ? hex.substring(1) : hex;
        if (digits.length() != 6) {
            throw new IllegalArgumentException("Expected a 6-digit hex color such as #ff8800, got: " + hex);
        }
        int value;
        try {
            value = HexFormat.fromHexDigits(digits);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Expected a 6-digit hex color such as #ff8800, got: " + hex, e);
        }
        return new Rgb((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF);
    }

    /**
     * Returns a color from the 256-color palette.
     *
     * @param index palette index, 0–255
     * @return the color
     */
    static Indexed indexed(int index) {
        return new Indexed(index);
    }

    /**
     * Parses a color by name or notation: {@code "red"}, {@code "bright_blue"},
     * {@code "#ff8800"}, {@code "rgb(255,136,0)"} or {@code "color(208)"}.
     *
     * Names and notations are case-insensitive, and spaces are allowed inside the parentheses.
     *
     * @param definition the color definition
     * @return the color
     * @throws IllegalArgumentException if the definition is not recognised
     */
    static Color parse(String definition) {
        Objects.requireNonNull(definition, "definition");
        String value = definition.strip().toLowerCase(Locale.ROOT);
        if (value.startsWith("#")) {
            return hex(value);
        }
        String rgbArguments = arguments(value, "rgb");
        if (rgbArguments != null) {
            String[] channels = rgbArguments.split(",", -1);
            if (channels.length == 3) {
                try {
                    return rgb(Integer.parseInt(channels[0].strip()),
                            Integer.parseInt(channels[1].strip()),
                            Integer.parseInt(channels[2].strip()));
                } catch (NumberFormatException e) {
                    // reported below
                }
            }
            throw new IllegalArgumentException("Expected rgb(red,green,blue) with numbers 0-255, got: " + definition);
        }
        String colorArguments = arguments(value, "color");
        if (colorArguments != null) {
            try {
                return indexed(Integer.parseInt(colorArguments.strip()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected color(index) with a number 0-255, got: " + definition, e);
            }
        }
        for (Standard standard : Standard.values()) {
            if (standard.name().toLowerCase(Locale.ROOT).equals(value)) {
                return standard;
            }
        }
        throw new IllegalArgumentException("Unknown color: " + definition);
    }

    /** Returns what is inside {@code function(...)}, or {@code null} if {@code value} is not that call. */
    private static String arguments(String value, String function) {
        return value.startsWith(function + "(") && value.endsWith(")")
                ? value.substring(function.length() + 1, value.length() - 1)
                : null;
    }

    /**
     * Returns the closest color that can be displayed at the given depth.
     *
     * <p>True colors map to the nearest entry of the 256-color cube or grayscale ramp, and
     * 256-palette or true colors map to the nearest of the 16 standard colors. Colors the depth
     * can already display are returned unchanged.
     *
     * <pre>{@code
     * Color.rgb(255, 136, 0).downgrade(Color.Depth.EIGHT_BIT);  // Color.indexed(208)
     * Color.indexed(196).downgrade(Color.Depth.STANDARD);       // Color.Standard.BRIGHT_RED
     * }</pre>
     *
     * @param depth the color depth to fit into
     * @return the displayable color, or {@code null} if {@code depth} is {@link Depth#NONE NONE}
     */
    default Color downgrade(Depth depth) {
        Objects.requireNonNull(depth, "depth");
        switch (depth) {
            case NONE:
                return null;
            case STANDARD:
                if (this instanceof Standard) {
                    return this;
                }
                if (this instanceof Indexed indexed && indexed.index() < 16) {
                    return Standard.values()[indexed.index()];
                }
                int[] rgb = this instanceof Rgb true24
                        ? new int[] {true24.red(), true24.green(), true24.blue()}
                        : Palette.rgb(((Indexed) this).index());
                return Standard.values()[Palette.nearestStandard(rgb[0], rgb[1], rgb[2])];
            case EIGHT_BIT:
                return this instanceof Rgb true24
                        ? new Indexed(Palette.nearestIndexed(true24.red(), true24.green(), true24.blue()))
                        : this;
            default:
                return this;
        }
    }

    /** The 16 standard ANSI colors. Their exact appearance depends on the terminal theme. */
    enum Standard implements Color {
        BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE,
        BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW,
        BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYAN, BRIGHT_WHITE;

        /**
         * Returns this color's position in the ANSI palette, 0–15.
         *
         * @return the palette index
         */
        public int index() {
            return ordinal();
        }
    }

    /**
     * A color from the xterm 256-color palette.
     *
     * @param index palette index, 0–255
     */
    record Indexed(int index) implements Color {
        public Indexed {
            if (index < 0 || index > 255) {
                throw new IllegalArgumentException("Palette index must be 0-255, got " + index);
            }
        }
    }

    /**
     * A 24-bit true color.
     *
     * @param red   red channel, 0–255
     * @param green green channel, 0–255
     * @param blue  blue channel, 0–255
     */
    record Rgb(int red, int green, int blue) implements Color {
        public Rgb {
            requireChannel("red", red);
            requireChannel("green", green);
            requireChannel("blue", blue);
        }

        /**
         * Returns this color as a lowercase hex string such as {@code "#ff8800"}.
         *
         * @return the hex representation
         */
        public String toHex() {
            return String.format("#%02x%02x%02x", red, green, blue);
        }

        private static void requireChannel(String name, int value) {
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException(name + " must be 0-255, got " + value);
            }
        }
    }

    /** How many colors a terminal can display, from least to most capable. */
    enum Depth {
        /** No styling at all: output is plain text without any escape sequences. */
        NONE,
        /** The 16 {@link Standard} colors. */
        STANDARD,
        /** The 256-color {@link Indexed} palette. */
        EIGHT_BIT,
        /** 24-bit {@link Rgb} color. */
        TRUE_COLOR
    }
}
