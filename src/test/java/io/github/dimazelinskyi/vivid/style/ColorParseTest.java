package io.github.dimazelinskyi.vivid.style;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColorParseTest {

    @Test
    void parsesStandardNames() {
        assertEquals(Color.RED, Color.parse("red"));
        assertEquals(Color.Standard.BRIGHT_BLUE, Color.parse("bright_blue"));
        assertEquals(Color.RED, Color.parse(" Red "));
    }

    @Test
    void parsesHex() {
        assertEquals(Color.rgb(255, 136, 0), Color.parse("#ff8800"));
        assertEquals(Color.rgb(255, 136, 0), Color.parse("#FF8800"));
    }

    @Test
    void parsesRgb() {
        assertEquals(Color.rgb(255, 136, 0), Color.parse("rgb(255,136,0)"));
        assertEquals(Color.rgb(255, 136, 0), Color.parse("RGB( 255 , 136 , 0 )"));
    }

    @Test
    void parsesPaletteIndex() {
        assertEquals(Color.indexed(208), Color.parse("color(208)"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "reddish", "ff8800", "#12345", "rgb(1,2)", "rgb(a,b,c)", "rgb(300,0,0)",
        "color(256)", "color(x)", "color()"})
    void rejectsInvalidDefinitions(String definition) {
        assertThrows(IllegalArgumentException.class, () -> Color.parse(definition));
    }
}
