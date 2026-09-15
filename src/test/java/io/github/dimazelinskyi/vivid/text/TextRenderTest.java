package io.github.dimazelinskyi.vivid.text;

import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextRenderTest {

    private static final String RED = "[31m";
    private static final String BOLD_RED = "[1;31m";
    private static final String RESET = "[0m";

    private static final Style BOLD = Style.builder().bold().build();

    private static List<String> render(Text text, int width, Color.Depth depth) {
        return text.render(new Renderable.Context(width, depth));
    }

    private static List<String> render(Text text) {
        return render(text, 80, Color.Depth.STANDARD);
    }

    @Nested
    class Styling {

        @Test
        void plainTextHasNoEscapes() {
            assertEquals(List.of("hello"), render(Text.of("hello")));
        }

        @Test
        void baseStyleWrapsTheLine() {
            assertEquals(List.of(RED + "OK" + RESET), render(Text.styled("OK", Style.of(Color.RED))));
        }

        @Test
        void spansLayerOnTopOfTheBaseStyle() {
            Text text = Text.styled("abc", Style.of(Color.RED)).stylize(BOLD, 1, 2);

            assertEquals(List.of(RED + "a" + RESET + BOLD_RED + "b" + RESET + RED + "c" + RESET), render(text));
        }

        @Test
        void unstyledRunsAreLeftBare() {
            Text text = Text.of("Status: ").append("OK", Style.of(Color.RED));

            assertEquals(List.of("Status: " + RED + "OK" + RESET), render(text));
        }

        @Test
        void adjacentRunsWithTheSameStyleMerge() {
            Text text = Text.of("ab").stylize(BOLD, 0, 1).stylize(BOLD, 1, 2);

            assertEquals(List.of("[1mab" + RESET), render(text));
        }

        @Test
        void noColorDepthRendersPlainText() {
            Text text = Text.styled("OK", Style.of(Color.RED)).stylize(BOLD, 0, 1);

            assertEquals(List.of("OK"), render(text, 80, Color.Depth.NONE));
        }
    }

    @Nested
    class Lines {

        @Test
        void eachLineResetsItsStyle() {
            assertEquals(List.of(RED + "a" + RESET, RED + "b" + RESET), render(Text.styled("a\nb", Style.of(Color.RED))));
        }

        @Test
        void emptyTextIsOneEmptyLine() {
            assertEquals(List.of(""), render(Text.of("")));
        }

        @Test
        void trailingNewlineAddsAnEmptyLine() {
            assertEquals(List.of("a", ""), render(Text.of("a\n")));
        }
    }

    @Nested
    class Justification {

        @Test
        void leftAddsNoPadding() {
            assertEquals(List.of("ab"), render(Text.of("ab"), 6, Color.Depth.NONE));
        }

        @Test
        void rightPadsToTheWidth() {
            assertEquals(List.of("    ab"), render(Text.of("ab").withJustify(Justify.RIGHT), 6, Color.Depth.NONE));
        }

        @Test
        void centerPadsOnTheLeftOnly() {
            assertEquals(List.of("  ab"), render(Text.of("ab").withJustify(Justify.CENTER), 6, Color.Depth.NONE));
        }

        @Test
        void linesWiderThanTheWidthAreLeftAsIs() {
            assertEquals(List.of("abcdef"), render(Text.of("abcdef").withJustify(Justify.RIGHT), 3, Color.Depth.NONE));
        }
    }
}
