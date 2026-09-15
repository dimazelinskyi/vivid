package io.github.dimazelinskyi.vivid;

import io.github.dimazelinskyi.vivid.panel.Panel;
import io.github.dimazelinskyi.vivid.progress.ProgressBar;
import io.github.dimazelinskyi.vivid.progress.Spinner;
import io.github.dimazelinskyi.vivid.render.BoxStyle;
import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Attribute;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.table.Column;
import io.github.dimazelinskyi.vivid.table.Table;
import io.github.dimazelinskyi.vivid.text.Markup;
import io.github.dimazelinskyi.vivid.text.Text;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the public API skeleton. These pin down the shape and invariants of the value types;
 * rendering tests will follow once rendering is implemented.
 */
class VividTest {

    @Test
    void facadeSharesOneDefaultConsole() {
        assertSame(Vivid.console(), Vivid.console());
    }

    @Nested
    class Styles {

        @Test
        void builderProducesImmutableStyle() {
            Style style = Vivid.style().color(Color.RED).on(Color.WHITE).bold().italic().build();

            assertEquals(Color.RED, style.foreground());
            assertEquals(Color.WHITE, style.background());
            assertTrue(style.has(Attribute.BOLD));
            assertTrue(style.has(Attribute.ITALIC));
            assertFalse(style.has(Attribute.UNDERLINE));
            assertThrows(UnsupportedOperationException.class, () -> style.attributes().add(Attribute.DIM));
        }

        @Test
        void combineLetsTheOverlayWinButKeepsAllAttributes() {
            Style base = Style.builder().color(Color.RED).on(Color.BLACK).bold().build();
            Style overlay = Style.builder().color(Color.GREEN).underline().build();

            Style combined = base.combine(overlay);

            assertEquals(Color.GREEN, combined.foreground());
            assertEquals(Color.BLACK, combined.background());
            assertTrue(combined.has(Attribute.BOLD));
            assertTrue(combined.has(Attribute.UNDERLINE));
        }

        @Test
        void noneIsPlain() {
            assertTrue(Style.NONE.isPlain());
            assertFalse(Style.of(Color.BLUE).isPlain());
        }
    }

    @Nested
    class Colors {

        @Test
        void parsesHexWithOrWithoutHash() {
            assertEquals(Color.rgb(255, 136, 0), Color.hex("#ff8800"));
            assertEquals(Color.rgb(255, 136, 0), Color.hex("FF8800"));
            assertEquals("#1e90ff", Color.rgb(30, 144, 255).toHex());
        }

        @Test
        void rejectsInvalidValues() {
            assertThrows(IllegalArgumentException.class, () -> Color.hex("#ff88"));
            assertThrows(IllegalArgumentException.class, () -> Color.hex("#-12345"));
            assertThrows(IllegalArgumentException.class, () -> Color.hex("#gggggg"));
            assertThrows(IllegalArgumentException.class, () -> Color.rgb(256, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> Color.indexed(-1));
        }
    }

    @Nested
    class Texts {

        @Test
        void appendTracksStyledSpans() {
            Style green = Style.of(Color.GREEN);

            Text text = Text.of("Status: ").append("OK", green);

            assertEquals("Status: OK", text.plain());
            assertEquals(List.of(new Text.Span(8, 10, green)), text.spans());
        }

        @Test
        void appendShiftsSpansOfTheAppendedText() {
            Style bold = Style.builder().bold().build();
            Text tail = Text.of("ab").stylize(bold, 1, 2);

            Text text = Text.of("xyz").append(tail);

            assertEquals(List.of(new Text.Span(4, 5, bold)), text.spans());
        }

        @Test
        void escapesMarkup() {
            assertEquals("\\[not a tag]", Markup.escape("[not a tag]"));
        }
    }

    @Nested
    class Tables {

        @Test
        void builderCollectsColumnsAndRows() {
            Table table = Vivid.table("Name", "Version")
                    .column(Column.of("Size").right())
                    .title("Dependencies")
                    .row("vivid", "0.1.0", "48 KB")
                    .row("junit", "5.13.4")
                    .build();

            assertEquals("Dependencies", table.title());
            assertEquals(List.of("Name", "Version", "Size"),
                    table.columns().stream().map(Column::header).toList());
            assertEquals(Justify.RIGHT, table.columns().get(2).justify());
            assertEquals(2, table.rows().size());
            assertEquals(BoxStyle.ROUNDED, table.box());
        }

        @Test
        void rejectsRowsWiderThanTheTable() {
            Table.Builder builder = Vivid.table("Only").row("a", "b");

            assertThrows(IllegalArgumentException.class, builder::build);
        }

        @Test
        void columnWithersDoNotMutate() {
            Column column = Column.of("Price");
            Column right = column.right().withWidth(10);

            assertEquals(Justify.LEFT, column.justify());
            assertEquals(Justify.RIGHT, right.justify());
            assertEquals(10, right.minWidth());
            assertEquals(10, right.maxWidth());
        }
    }

    @Nested
    class Panels {

        @Test
        void builderAppliesDefaultsAndOverrides() {
            Panel panel = Vivid.panel("Hello").title("Greeting").padding(1, 2).build();

            assertEquals("Greeting", panel.title());
            assertNull(panel.subtitle());
            assertEquals(new Panel.Padding(1, 2, 1, 2), panel.padding());
            assertEquals(BoxStyle.ROUNDED, panel.box());
        }
    }

    @Nested
    class ProgressAndSpinners {

        @Test
        void progressBarReportsPercentage() {
            ProgressBar bar = Vivid.progressBar(200).withCompleted(50);

            assertEquals(25.0, bar.percentage());
            assertFalse(bar.isFinished());
            assertTrue(bar.advance(150).isFinished());
            assertEquals(100.0, bar.advance(1_000).percentage());
        }

        @Test
        void indeterminateBarHasNoPercentage() {
            ProgressBar bar = ProgressBar.of(0).advance(10);

            assertTrue(bar.isIndeterminate());
            assertFalse(bar.isFinished());
            assertEquals(0.0, bar.percentage());
        }

        @Test
        void spinnerPicksFrameFromElapsedTime() {
            Spinner spinner = Spinner.of(Spinner.Type.LINE);

            assertEquals("-", spinner.frameAt(Duration.ZERO));
            assertEquals("\\", spinner.frameAt(Duration.ofMillis(130)));
            assertEquals("-", spinner.frameAt(Duration.ofMillis(4 * 130)));
        }
    }

    @Test
    void renderingIsNotImplementedYet() {
        Renderable.Context context = Renderable.Context.of(80);

        assertThrows(UnsupportedOperationException.class, () -> Text.of("hi").render(context));
        assertThrows(UnsupportedOperationException.class, () -> Vivid.table("A").build().render(context));
    }

    public static void main(String[] args) {

        Vivid.console().info("Hello world!");
    }
}
