package io.github.dimazelinskyi.vivid.text;

import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkupTest {

    private static final Style BOLD = Style.builder().bold().build();
    private static final Style RED = Style.of(Color.RED);

    private static void assertParsed(String markup, String plain, Text.Span... spans) {
        Text text = Markup.parse(markup);
        assertEquals(plain, text.plain());
        assertEquals(List.of(spans), text.spans());
    }

    @Nested
    class Tags {

        @Test
        void plainTextIsUnchanged() {
            assertParsed("hello", "hello");
        }

        @Test
        void namedClosingTag() {
            assertParsed("[bold]a[/bold]b", "ab", new Text.Span(0, 1, BOLD));
        }

        @Test
        void anonymousClosingTag() {
            assertParsed("[red]a[/]b", "ab", new Text.Span(0, 1, RED));
        }

        @Test
        void nestedTagsLayerInnerOnTop() {
            assertParsed("[bold]a[red]b[/red][/bold]", "ab", new Text.Span(0, 2, BOLD), new Text.Span(1, 2, RED));

            List<String> rendered = Markup.parse("[red]a[blue]b[/blue][/red]")
                    .render(new Renderable.Context(80, Color.Depth.STANDARD));
            assertEquals(List.of("\u001B[31ma\u001B[0m\u001B[34mb\u001B[0m"), rendered);
        }

        @Test
        void namedCloseCanEndAnOuterTag() {
            assertParsed("[bold]a[red]b[/bold]c[/red]", "abc", new Text.Span(0, 2, BOLD), new Text.Span(1, 3, RED));
        }

        @Test
        void closingTagMatchesTheStyleNotTheSpelling() {
            Style boldRed = Style.builder().bold().color(Color.RED).build();

            assertParsed("[bold red]a[/RED  bold]", "a", new Text.Span(0, 1, boldRed));
        }

        @Test
        void unclosedTagsEndWithTheText() {
            assertParsed("[green]OK", "OK", new Text.Span(0, 2, Style.of(Color.GREEN)));
        }

        @Test
        void emptyTagsAddNoSpans() {
            assertParsed("[bold][/bold]x", "x");
        }
    }

    @Nested
    class Literals {

        @ParameterizedTest
        @ValueSource(strings = {"[INFO] started", "list [1, 2]", "[]", "a [ b", "a ] b", "[/usr/bin]", "[bold"})
        void bracketsWithoutAStyleStayLiteral(String markup) {
            assertParsed(markup, markup);
        }

        @Test
        void escapedBracketIsLiteral() {
            assertParsed("\\[bold]x", "[bold]x");
        }

        @Test
        void backslashElsewhereIsLiteral() {
            assertParsed("C:\\temp", "C:\\temp");
        }

        @ParameterizedTest
        @ValueSource(strings = {"[bold]x[/]", "C:\\[x]", "a[red]", "\\[", "[/]"})
        void escapeRoundTrips(String text) {
            assertParsed(Markup.escape(text), text);
        }
    }

    @Nested
    class Errors {

        @Test
        void anonymousCloseWithNothingOpen() {
            assertThrows(IllegalArgumentException.class, () -> Markup.parse("a [/] b"));
        }

        @Test
        void namedCloseWithoutMatchingOpen() {
            IllegalArgumentException error =
                    assertThrows(IllegalArgumentException.class, () -> Markup.parse("[bold]a[/red]"));

            assertTrue(error.getMessage().contains("[/red]"), error.getMessage());
        }
    }

    @Test
    void stripReturnsPlainText() {
        assertEquals("Hi there", Markup.strip("[bold]Hi[/] there"));
    }

    @Test
    void textMarkupDelegatesToParse() {
        assertEquals(Markup.parse("[red]x[/]"), Text.markup("[red]x[/]"));
    }
}
