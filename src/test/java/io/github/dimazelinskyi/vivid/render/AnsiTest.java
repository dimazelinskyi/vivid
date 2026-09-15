package io.github.dimazelinskyi.vivid.render;

import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnsiTest {

    private static final String ESC = "[";

    @Test
    void plainStyleEmitsNothing() {
        assertEquals("", Ansi.sgr(Style.NONE, Color.Depth.TRUE_COLOR));
    }

    @Test
    void noColorDepthEmitsNothing() {
        Style style = Style.builder().color(Color.RED).bold().build();

        assertEquals("", Ansi.sgr(style, Color.Depth.NONE));
    }

    @Test
    void attributesComeBeforeColors() {
        Style style = Style.builder().color(Color.RED).on(Color.BLUE).underline().bold().build();

        assertEquals(ESC + "1;4;31;44m", Ansi.sgr(style, Color.Depth.STANDARD));
    }

    @Test
    void brightColorsUseHighIntensityCodes() {
        Style style = Style.builder().color(Color.Standard.BRIGHT_RED).on(Color.Standard.BRIGHT_WHITE).build();

        assertEquals(ESC + "91;107m", Ansi.sgr(style, Color.Depth.STANDARD));
    }

    @Test
    void paletteColors() {
        Style style = Style.builder().color(Color.indexed(208)).on(Color.indexed(17)).build();

        assertEquals(ESC + "38;5;208;48;5;17m", Ansi.sgr(style, Color.Depth.EIGHT_BIT));
    }

    @Test
    void trueColors() {
        Style style = Style.builder().color(Color.rgb(1, 2, 3)).on(Color.hex("#ff8800")).build();

        assertEquals(ESC + "38;2;1;2;3;48;2;255;136;0m", Ansi.sgr(style, Color.Depth.TRUE_COLOR));
    }

    @Test
    void stripRemovesEscapeSequences() {
        assertEquals("Status: OK", Ansi.strip("Status: " + ESC + "1;38;2;1;2;3mOK" + Ansi.RESET));
    }

    @Test
    void colorsAreDowngradedToTheDepth() {
        Style style = Style.of(Color.rgb(255, 136, 0));

        assertEquals(ESC + "38;5;208m", Ansi.sgr(style, Color.Depth.EIGHT_BIT));
    }
}
