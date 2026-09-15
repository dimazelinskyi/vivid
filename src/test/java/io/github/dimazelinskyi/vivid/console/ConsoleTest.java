package io.github.dimazelinskyi.vivid.console;

import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.text.Text;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private Console.Builder builder() {
        return Console.builder().output(new PrintStream(buffer, true, UTF_8)).environment(Map.of());
    }

    private String output() {
        return buffer.toString(UTF_8);
    }

    @Nested
    class Printing {

        @Test
        void printlnRendersStyledText() {
            builder().colorDepth(Color.Depth.STANDARD).build()
                    .println(Text.styled("OK", Style.of(Color.GREEN)));

            assertEquals("[32mOK[0m\n", output());
        }

        @Test
        void printSeparatesObjectsWithSpaces() {
            builder().build().print("a", 1, Text.of("b"));

            assertEquals("a 1 b", output());
        }

        @Test
        void multiLineObjectsJoinAtTheirBoundary() {
            builder().build().println(Text.of("a\nb"), "c");

            assertEquals("a\nb c\n", output());
        }

        @Test
        void laterObjectsOnlyGetTheRemainingWidth() {
            builder().width(6).colorDepth(Color.Depth.STANDARD).build()
                    .println(Text.styled("a", Style.of(Color.RED)), Text.of("b").withJustify(Justify.RIGHT));

            assertEquals("[31ma[0m    b\n", output());
        }

        @Test
        void aFullLineLeavesAtLeastOneCell() {
            builder().width(3).build().println("abc", Text.of("d").withJustify(Justify.RIGHT));

            assertEquals("abc d\n", output());
        }

        @Test
        void newLinePrintsAnEmptyLine() {
            builder().build().newLine();

            assertEquals("\n", output());
        }
    }

    @Nested
    class Detection {

        @Test
        void redirectedOutputIsNotATerminalAndHasNoColor() {
            Console console = builder().environment(Map.of("COLORTERM", "truecolor")).build();
            console.println(Text.styled("OK", Style.of(Color.GREEN)));

            assertFalse(console.isTerminal());
            assertEquals(Color.Depth.NONE, console.colorDepth());
            assertEquals("OK\n", output());
        }

        @Test
        void forceColorEnablesColorInPipes() {
            Console console = builder().environment(Map.of("FORCE_COLOR", "1")).build();

            assertEquals(Color.Depth.STANDARD, console.colorDepth());
        }

        @Test
        void forcedTerminalDetectsColorFromTheEnvironment() {
            Console console = builder().forceTerminal(true).environment(Map.of("TERM", "xterm-256color")).build();

            assertTrue(console.isTerminal());
            assertEquals(Color.Depth.EIGHT_BIT, console.colorDepth());
        }

        @Test
        void widthComesFromColumns() {
            assertEquals(100, builder().environment(Map.of("COLUMNS", "100")).build().width());
        }

        @Test
        void configuredValuesWinOverDetection() {
            Console console = builder()
                    .environment(Map.of("COLUMNS", "120", "FORCE_COLOR", "3"))
                    .width(40)
                    .noColor()
                    .build();

            assertEquals(40, console.width());
            assertEquals(Color.Depth.NONE, console.colorDepth());
        }
    }
}
