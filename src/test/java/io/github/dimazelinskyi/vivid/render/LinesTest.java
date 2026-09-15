package io.github.dimazelinskyi.vivid.render;

import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinesTest {

    private static final String RED = "[31m";
    private static final String RESET = Ansi.RESET;

    @Test
    void widthIgnoresEscapeSequences() {
        assertEquals(5, Lines.width(RED + "ab" + RESET + "cde"));
    }

    @Test
    void fitPadsShortLines() {
        assertEquals(RED + "ab" + RESET + "  ", Lines.fit(RED + "ab" + RESET, 4));
    }

    @Test
    void fitTruncatesLongLines() {
        assertEquals("abc…", Lines.fit("abcdef", 4));
    }

    @Test
    void truncateLeavesFittingLinesAlone() {
        assertEquals("abc", Lines.truncate("abc", 5));
    }

    @Test
    void truncateInsideAStyleKeepsTheStyleAndResets() {
        assertEquals(RED + "abc…" + RESET, Lines.truncate(RED + "abcdef" + RESET, 4));
    }

    @Test
    void truncateAfterAStyleEndsKeepsTheReset() {
        assertEquals(RED + "abc" + RESET + "…", Lines.truncate(RED + "abc" + RESET + "def", 4));
    }

    @Test
    void truncateToTinyWidths() {
        assertEquals(RED + "…" + RESET, Lines.truncate(RED + "abc" + RESET, 1));
        assertEquals("", Lines.truncate("abc", 0));
    }

    @Test
    void styledWrapsTextInStyleAndReset() {
        assertEquals(RED + "x" + RESET, Ansi.styled("x", Style.of(Color.RED), Color.Depth.STANDARD));
        assertEquals("x", Ansi.styled("x", Style.of(Color.RED), Color.Depth.NONE));
        assertEquals("", Ansi.styled("", Style.of(Color.RED), Color.Depth.STANDARD));
    }
}
