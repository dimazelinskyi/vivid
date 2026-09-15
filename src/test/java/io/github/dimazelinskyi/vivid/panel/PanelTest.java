package io.github.dimazelinskyi.vivid.panel;

import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanelTest {

    private static List<String> render(Panel panel, int width) {
        return panel.render(new Renderable.Context(width, Color.Depth.NONE));
    }

    private static String line(int count) {
        return "─".repeat(count);
    }

    @Test
    void expandsToTheFullWidthByDefault() {
        assertEquals(List.of(
                "╭" + line(10) + "╮",
                "│ Hello    │",
                "╰" + line(10) + "╯"),
                render(Panel.of("Hello"), 12));
    }

    @Test
    void fitsContentWithTitleSubtitleAndPadding() {
        Panel panel = Panel.builder("Deployed [bold]v1.4.2[/]")
                .title("Release")
                .subtitle("now")
                .padding(1, 2)
                .expand(false)
                .build();

        assertEquals(List.of(
                "╭" + line(5) + " Release " + line(5) + "╮",
                "│" + " ".repeat(19) + "│",
                "│  Deployed v1.4.2  │",
                "│" + " ".repeat(19) + "│",
                "╰" + line(7) + " now " + line(7) + "╯"),
                render(panel, 80));
    }

    @Test
    void titleCanBeAlignedLeftOrRight() {
        Panel left = Panel.builder("x").title("T").titleJustify(Justify.LEFT).build();
        Panel right = Panel.builder("x").title("T").titleJustify(Justify.RIGHT).build();

        assertEquals("╭─ T " + line(5) + "╮", render(left, 11).get(0));
        assertEquals("╭" + line(5) + " T ─╮", render(right, 11).get(0));
    }

    @Test
    void fittedPanelIsWideEnoughForItsTitle() {
        Panel panel = Panel.builder("x").title("Long title").expand(false).build();

        assertEquals("╭─ Long title ─╮", render(panel, 80).get(0));
        assertEquals("│ x            │", render(panel, 80).get(1));
    }

    @Test
    void fittedPanelIsCappedAtTheAvailableWidth() {
        Panel panel = Panel.builder("a very long line of content").expand(false).build();

        assertEquals(List.of("╭────────╮", "│ a ver… │", "╰────────╯"), render(panel, 10));
    }

    @Test
    void longTitlesAreTruncated() {
        Panel panel = Panel.builder("x").title("Very long title").build();

        assertEquals("╭─ Ver… ─╮", render(panel, 10).get(0));
    }

    @Test
    void multiLineContent() {
        assertEquals(List.of("╭───╮", "│ a │", "│ b │", "╰───╯"), render(Panel.builder(Text.of("a\nb")).expand(false).build(), 80));
    }

    @Test
    void borderStyleColorsOnlyTheBorder() {
        Panel panel = Panel.builder("x").borderStyle(Style.of(Color.RED)).expand(false).build();

        String red = "[31m";
        String reset = "[0m";
        assertEquals(List.of(
                red + "╭───╮" + reset,
                red + "│" + reset + " x " + red + "│" + reset,
                red + "╰───╯" + reset),
                panel.render(new Renderable.Context(80, Color.Depth.STANDARD)));
    }
}
