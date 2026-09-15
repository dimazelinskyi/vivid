package io.github.dimazelinskyi.vivid.style;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleParseTest {

    @Test
    void parsesAttributesAndColors() {
        Style expected = Style.builder().bold().italic().color(Color.RED).on(Color.hex("#202020")).build();

        assertEquals(expected, Style.parse("bold italic red on #202020"));
    }

    @Test
    void isCaseInsensitiveAndIgnoresExtraSpaces() {
        assertEquals(Style.builder().bold().color(Color.RED).build(), Style.parse("  BOLD   Red "));
    }

    @Test
    void parsesBackgroundOnly() {
        assertEquals(Style.builder().on(Color.BLUE).build(), Style.parse("on blue"));
    }

    @Test
    void allowsSpacesInsideColorFunctions() {
        Style expected = Style.builder().underline().on(Color.rgb(30, 30, 30)).build();

        assertEquals(expected, Style.parse("underline on rgb(30, 30, 30)"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "bold on", "red blue", "on red on blue", "shiny", "bold on shiny"})
    void rejectsInvalidDefinitions(String definition) {
        assertThrows(IllegalArgumentException.class, () -> Style.parse(definition));
    }

    @Test
    void errorNamesTheUnknownWord() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> Style.parse("bold shiny"));

        assertTrue(error.getMessage().contains("'shiny'"), error.getMessage());
    }
}
