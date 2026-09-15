package io.github.dimazelinskyi.vivid.console;

import io.github.dimazelinskyi.vivid.style.Color;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalDetectorTest {

    private static Color.Depth depth(Map<String, String> environment, boolean interactive) {
        return new TerminalDetector(environment, interactive).colorDepth();
    }

    @Nested
    class ColorDepth {

        @Test
        void noColorWinsOverEverything() {
            assertEquals(Color.Depth.NONE, depth(Map.of("NO_COLOR", "1", "FORCE_COLOR", "3"), true));
        }

        @Test
        void emptyNoColorIsIgnored() {
            assertEquals(Color.Depth.STANDARD, depth(Map.of("NO_COLOR", ""), true));
        }

        @Test
        void redirectedOutputHasNoColor() {
            assertEquals(Color.Depth.NONE, depth(Map.of("COLORTERM", "truecolor"), false));
        }

        @Test
        void forceColorEnablesColorWhenRedirected() {
            assertEquals(Color.Depth.STANDARD, depth(Map.of("FORCE_COLOR", "1"), false));
            assertEquals(Color.Depth.STANDARD, depth(Map.of("FORCE_COLOR", ""), false));
            assertEquals(Color.Depth.EIGHT_BIT, depth(Map.of("FORCE_COLOR", "2"), false));
            assertEquals(Color.Depth.TRUE_COLOR, depth(Map.of("FORCE_COLOR", "3"), false));
        }

        @Test
        void forceColorKeepsARicherDetectedDepth() {
            assertEquals(Color.Depth.TRUE_COLOR, depth(Map.of("FORCE_COLOR", "1", "COLORTERM", "truecolor"), false));
        }

        @Test
        void forceColorZeroDisablesColor() {
            assertEquals(Color.Depth.NONE, depth(Map.of("FORCE_COLOR", "0"), true));
            assertEquals(Color.Depth.NONE, depth(Map.of("FORCE_COLOR", "false"), true));
        }

        @Test
        void dumbTerminalHasNoColor() {
            assertEquals(Color.Depth.NONE, depth(Map.of("TERM", "dumb"), true));
        }

        @Test
        void trueColorFromColorTermOrWindowsTerminal() {
            assertEquals(Color.Depth.TRUE_COLOR, depth(Map.of("COLORTERM", "truecolor"), true));
            assertEquals(Color.Depth.TRUE_COLOR, depth(Map.of("COLORTERM", "24bit"), true));
            assertEquals(Color.Depth.TRUE_COLOR, depth(Map.of("WT_SESSION", "abc"), true));
        }

        @Test
        void eightBitFrom256ColorTerm() {
            assertEquals(Color.Depth.EIGHT_BIT, depth(Map.of("TERM", "xterm-256color"), true));
        }

        @Test
        void standardByDefault() {
            assertEquals(Color.Depth.STANDARD, depth(Map.of("TERM", "xterm"), true));
            assertEquals(Color.Depth.STANDARD, depth(Map.of(), true));
        }
    }

    @Nested
    class Width {

        private int width(Map<String, String> environment) {
            return new TerminalDetector(environment, true).width();
        }

        @Test
        void usesColumns() {
            assertEquals(120, width(Map.of("COLUMNS", "120")));
        }

        @Test
        void fallsBackTo80() {
            assertEquals(80, width(Map.of()));
            assertEquals(80, width(Map.of("COLUMNS", "wide")));
            assertEquals(80, width(Map.of("COLUMNS", "0")));
        }
    }
}
