package io.github.dimazelinskyi.vivid.render;

import io.github.dimazelinskyi.vivid.style.Attribute;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * ANSI escape sequences for styled terminal output.
 *
 * <pre>{@code
 * String line = Ansi.sgr(style, Color.Depth.TRUE_COLOR) + "Done" + Ansi.RESET;
 * }</pre>
 */
public final class Ansi {

    /** Resets all colors and attributes to the terminal default. */
    public static final String RESET = "[0m";

    private Ansi() {
    }

    /**
     * Returns the SGR ("Select Graphic Rendition") sequence that switches the terminal to a style,
     * such as {@code ESC[1;31m} for bold red. Colors the depth cannot display are
     * {@link Color#downgrade(Color.Depth) downgraded} first.
     *
     * @param style the style to apply
     * @param depth the richest color encoding to emit
     * @return the escape sequence, or an empty string if the style is plain or {@code depth} is
     *         {@link Color.Depth#NONE NONE}
     */
    public static String sgr(Style style, Color.Depth depth) {
        Objects.requireNonNull(style, "style");
        Objects.requireNonNull(depth, "depth");
        if (depth == Color.Depth.NONE || style.isPlain()) {
            return "";
        }
        StringJoiner codes = new StringJoiner(";", "[", "m");
        for (Attribute attribute : style.attributes()) {
            codes.add(Integer.toString(attribute.sgrCode()));
        }
        addColor(codes, style.foreground(), depth, false);
        addColor(codes, style.background(), depth, true);
        return codes.toString();
    }

    private static void addColor(StringJoiner codes, Color color, Color.Depth depth, boolean background) {
        if (color == null) {
            return;
        }
        Color shown = color.downgrade(depth);
        if (shown instanceof Color.Standard standard) {
            int index = standard.index();
            int base = index < 8 ? (background ? 40 : 30) : (background ? 100 : 90);
            codes.add(Integer.toString(base + index % 8));
        } else if (shown instanceof Color.Indexed indexed) {
            codes.add((background ? "48;5;" : "38;5;") + indexed.index());
        } else if (shown instanceof Color.Rgb rgb) {
            codes.add((background ? "48;2;" : "38;2;") + rgb.red() + ";" + rgb.green() + ";" + rgb.blue());
        }
    }
}
