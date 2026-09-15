package io.github.dimazelinskyi.vivid.table;

import io.github.dimazelinskyi.vivid.render.BoxStyle;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.text.Text;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableTest {

    private static final String BOLD = "[1m";
    private static final String BOLD_RED = "[1;31m";
    private static final String RED = "[31m";
    private static final String RESET = "[0m";

    private static List<String> render(Table table, int width) {
        return table.render(new Renderable.Context(width, Color.Depth.NONE));
    }

    private static List<String> renderInColor(Table table) {
        return table.render(new Renderable.Context(80, Color.Depth.STANDARD));
    }

    @Nested
    class Layout {

        @Test
        void headerIsBoldByDefault() {
            Table table = Table.builder().columns("A").row("x").build();

            assertEquals(List.of("╭───╮", "│ " + BOLD + "A" + RESET + " │", "├───┤", "│ x │", "╰───╯"), renderInColor(table));
        }

        @Test
        void headerCanBeHidden() {
            Table table = Table.builder().columns("A", "B").row("x", "y").showHeader(false).build();

            assertEquals(List.of("╭───┬───╮", "│ x │ y │", "╰───┴───╯"), render(table, 80));
        }

        @Test
        void noBorderBox() {
            Table table = Table.builder().columns("A", "B").row("x", "yy").box(BoxStyle.NONE).build();

            assertEquals(List.of(" A  B  ", " x  yy "), render(table, 80));
        }

        @Test
        void missingCellsAreBlank() {
            Table table = Table.builder().columns("A", "B").row("x").showHeader(false).build();

            assertEquals("│ x │   │", render(table, 80).get(1));
        }

        @Test
        void multiLineCellsMakeTallRows() {
            Table table = Table.builder().columns("A", "B").row(Text.of("x\ny"), "z").showHeader(false).build();

            assertEquals(List.of("╭───┬───╮", "│ x │ z │", "│ y │   │", "╰───┴───╯"), render(table, 80));
        }

        @Test
        void columnAlignment() {
            Table table = Table.builder().column(Column.of("N").right()).row("1").row("100").build();

            assertEquals(List.of("╭─────╮", "│   N │", "├─────┤", "│   1 │", "│ 100 │", "╰─────╯"), render(table, 80));
        }

        @Test
        void titleIsCenteredAndTruncatedToTheTable() {
            Table table = Table.builder().title("A long title").columns("A").build();

            assertEquals("A lo…", render(table, 80).get(0));
        }
    }

    @Nested
    class Widths {

        @Test
        void widestColumnsShrinkFirstToFit() {
            Table table = Table.builder().columns("Name", "Description").row("vivid", "Beautiful terminal output").build();

            assertEquals(List.of(
                    "╭───────┬──────────╮",
                    "│ Name  │ Descrip… │",
                    "├───────┼──────────┤",
                    "│ vivid │ Beautif… │",
                    "╰───────┴──────────╯"),
                    render(table, 20));
        }

        @Test
        void minAndMaxWidthBoundTheColumn() {
            Table table = Table.builder()
                    .column(Column.of("A").withWidth(5, 5))
                    .column(Column.of("B").withWidth(Column.AUTO, 3))
                    .row("x", "abcdef")
                    .showHeader(false)
                    .build();

            assertEquals("│ x     │ ab… │", render(table, 80).get(1));
        }

        @Test
        void expandSharesOutExtraWidth() {
            Table table = Table.builder().columns("A", "B").expand(true).build();

            assertEquals("│ A  │ B  │", render(table, 11).get(1));
        }
    }

    @Nested
    class Styling {

        @Test
        void textCellsGetRowThenColumnStyleBeneathTheirOwn() {
            Table table = Table.builder()
                    .column(Column.of("A").withStyle(Style.of(Color.RED)))
                    .row(Row.of("x").withStyle(Style.builder().bold().build()))
                    .row("[blue]y[/]")
                    .showHeader(false)
                    .build();

            List<String> lines = renderInColor(table);
            assertEquals("│ " + BOLD_RED + "x" + RESET + " │", lines.get(1));
            assertEquals("│ [34my" + RESET + " │", lines.get(2));
        }

        @Test
        void truncationKeepsEscapeSequencesIntact() {
            Table table = Table.builder().column(Column.of("A").withWidth(3)).row("[red]abcdef[/]").showHeader(false).build();

            assertEquals("│ " + RED + "ab…" + RESET + " │", renderInColor(table).get(1));
        }

        @Test
        void borderStyleColorsTheBorder() {
            Table table = Table.builder().columns("A").showHeader(false).row("x").borderStyle(Style.of(Color.RED)).build();

            assertEquals(List.of(RED + "╭───╮" + RESET, RED + "│" + RESET + " x " + RED + "│" + RESET, RED + "╰───╯" + RESET),
                    renderInColor(table));
        }
    }
}
