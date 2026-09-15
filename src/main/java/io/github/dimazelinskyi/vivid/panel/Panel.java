package io.github.dimazelinskyi.vivid.panel;

import io.github.dimazelinskyi.vivid.render.Ansi;
import io.github.dimazelinskyi.vivid.render.BoxStyle;
import io.github.dimazelinskyi.vivid.render.Justify;
import io.github.dimazelinskyi.vivid.render.Lines;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A box drawn around any {@link Renderable}, with an optional title and subtitle set into the border.
 *
 * <pre>{@code
 * Panel panel = Panel.builder("Deployed [bold]v1.4.2[/] to production")
 *         .title("Release")
 *         .subtitle("2 minutes ago")
 *         .borderStyle(Style.of(Color.GREEN))
 *         .padding(1, 2)
 *         .build();
 * }</pre>
 *
 * renders roughly as:
 *
 * <pre>
 * ╭────────────── Release ───────────────╮
 * │                                      │
 * │  Deployed v1.4.2 to production       │
 * │                                      │
 * ╰─────────── 2 minutes ago ────────────╯
 * </pre>
 *
 * @param content      what to draw inside the panel
 * @param title        text set into the top border, interpreted as markup; {@code null} for none
 * @param subtitle     text set into the bottom border, interpreted as markup; {@code null} for none
 * @param titleJustify alignment of the title and subtitle within the border
 * @param box          the border characters
 * @param borderStyle  style applied to the border
 * @param padding      space between the border and the content
 * @param expand       whether to stretch to the full available width ({@code true}) or fit the content
 */
public record Panel(
        Renderable content,
        String title,
        String subtitle,
        Justify titleJustify,
        BoxStyle box,
        Style borderStyle,
        Padding padding,
        boolean expand) implements Renderable {

    public Panel {
        Objects.requireNonNull(content, "content");
        titleJustify = titleJustify == null ? Justify.CENTER : titleJustify;
        box = box == null ? BoxStyle.ROUNDED : box;
        borderStyle = borderStyle == null ? Style.NONE : borderStyle;
        padding = padding == null ? Padding.DEFAULT : padding;
    }

    /**
     * Creates a panel with default settings.
     *
     * @param content the content; strings are interpreted as markup
     * @return the panel
     */
    public static Panel of(Object content) {
        return builder(content).build();
    }

    /**
     * Starts building a panel around the given content.
     *
     * @param content the content; strings are interpreted as markup
     * @return a fresh builder
     */
    public static Builder builder(Object content) {
        return new Builder(Renderable.from(content));
    }

    /**
     * Renders the border, title, subtitle, padding and content. The content is rendered at the
     * inner width, and lines that are still too long are truncated with an ellipsis.
     *
     * @param context the available width and color depth
     * @return the rendered lines
     */
    @Override
    public List<String> render(Renderable.Context context) {
        Objects.requireNonNull(context, "context");
        int frame = 2 + padding.left() + padding.right();
        int width = context.maxWidth();
        if (!expand) {
            int natural = Math.max(content.measure(context) + frame, Math.max(labelWidth(title), labelWidth(subtitle)));
            width = Math.min(width, natural);
        }
        int inner = Math.max(1, width - frame);
        width = inner + frame;

        List<String> lines = new ArrayList<>();
        lines.add(edge(box.topLeft(), title, box.topRight(), width, context.colorDepth()));

        String side = Ansi.styled(String.valueOf(box.vertical()), borderStyle, context.colorDepth());
        String blank = side + " ".repeat(width - 2) + side;
        String left = side + " ".repeat(padding.left());
        String right = " ".repeat(padding.right()) + side;
        for (int i = 0; i < padding.top(); i++) {
            lines.add(blank);
        }
        for (String line : content.render(new Renderable.Context(inner, context.colorDepth()))) {
            lines.add(left + Lines.fit(line, inner) + right);
        }
        for (int i = 0; i < padding.bottom(); i++) {
            lines.add(blank);
        }

        lines.add(edge(box.bottomLeft(), subtitle, box.bottomRight(), width, context.colorDepth()));
        return List.copyOf(lines);
    }

    /** The width a label needs in the border: corners, one line character and a space either side. */
    private static int labelWidth(String label) {
        return label == null || label.isEmpty() ? 0 : Text.markup(label).measure(Renderable.Context.of(1)) + 6;
    }

    /** Draws the top or bottom border, with the label set into it if there is room. */
    private String edge(char leftCorner, String label, char rightCorner, int width, Color.Depth depth) {
        int span = width - 2;
        String horizontal = String.valueOf(box.horizontal());
        int room = span - 4;
        if (label == null || label.isEmpty() || room < 1) {
            return Ansi.styled(leftCorner + horizontal.repeat(span) + rightCorner, borderStyle, depth);
        }
        List<String> rendered = Text.markup(label).render(new Renderable.Context(room, depth));
        String text = Lines.truncate(rendered.get(0), room);
        int fill = span - Lines.width(text) - 2;
        int before = switch (titleJustify) {
            case LEFT -> 1;
            case CENTER -> fill / 2;
            case RIGHT -> fill - 1;
        };
        return Ansi.styled(leftCorner + horizontal.repeat(before), borderStyle, depth)
                + " " + text + " "
                + Ansi.styled(horizontal.repeat(fill - before) + rightCorner, borderStyle, depth);
    }

    /**
     * Space, in terminal cells, between a panel's border and its content.
     *
     * @param top    blank lines above the content
     * @param right  blank columns right of the content
     * @param bottom blank lines below the content
     * @param left   blank columns left of the content
     */
    public record Padding(int top, int right, int bottom, int left) {

        /** No padding. */
        public static final Padding NONE = new Padding(0, 0, 0, 0);

        /** The default: no vertical padding, one column either side. */
        public static final Padding DEFAULT = new Padding(0, 1, 0, 1);

        public Padding {
            if (top < 0 || right < 0 || bottom < 0 || left < 0) {
                throw new IllegalArgumentException("Padding must not be negative");
            }
        }

        /**
         * Returns equal padding on all four sides.
         *
         * @param all the padding for every side
         * @return the padding
         */
        public static Padding of(int all) {
            return new Padding(all, all, all, all);
        }

        /**
         * Returns symmetric padding, CSS-style.
         *
         * @param vertical   the padding above and below
         * @param horizontal the padding left and right
         * @return the padding
         */
        public static Padding of(int vertical, int horizontal) {
            return new Padding(vertical, horizontal, vertical, horizontal);
        }
    }

    /**
     * A fluent builder for {@link Panel}. Mutable and not thread-safe.
     */
    public static final class Builder {

        private final Renderable content;
        private String title;
        private String subtitle;
        private Justify titleJustify = Justify.CENTER;
        private BoxStyle box = BoxStyle.ROUNDED;
        private Style borderStyle = Style.NONE;
        private Padding padding = Padding.DEFAULT;
        private boolean expand = true;

        private Builder(Renderable content) {
            this.content = content;
        }

        /**
         * Sets the title set into the top border.
         *
         * @param title the title, interpreted as markup
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the subtitle set into the bottom border.
         *
         * @param subtitle the subtitle, interpreted as markup
         * @return this builder
         */
        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * Sets the alignment of the title and subtitle. Defaults to centered.
         *
         * @param titleJustify the alignment
         * @return this builder
         */
        public Builder titleJustify(Justify titleJustify) {
            this.titleJustify = titleJustify;
            return this;
        }

        /**
         * Sets the border characters. Defaults to {@link BoxStyle#ROUNDED}.
         *
         * @param box the box style
         * @return this builder
         */
        public Builder box(BoxStyle box) {
            this.box = box;
            return this;
        }

        /**
         * Sets the style of the border.
         *
         * @param borderStyle the border style
         * @return this builder
         */
        public Builder borderStyle(Style borderStyle) {
            this.borderStyle = borderStyle;
            return this;
        }

        /**
         * Sets equal padding on all sides.
         *
         * @param all the padding for every side
         * @return this builder
         */
        public Builder padding(int all) {
            return padding(Padding.of(all));
        }

        /**
         * Sets symmetric padding.
         *
         * @param vertical   the padding above and below
         * @param horizontal the padding left and right
         * @return this builder
         */
        public Builder padding(int vertical, int horizontal) {
            return padding(Padding.of(vertical, horizontal));
        }

        /**
         * Sets the padding.
         *
         * @param padding the padding
         * @return this builder
         */
        public Builder padding(Padding padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Sets whether the panel fills the available width. Defaults to {@code true}.
         *
         * @param expand {@code false} to shrink the panel to fit its content
         * @return this builder
         */
        public Builder expand(boolean expand) {
            this.expand = expand;
            return this;
        }

        /**
         * Creates the immutable panel.
         *
         * @return the panel
         */
        public Panel build() {
            return new Panel(content, title, subtitle, titleJustify, box, borderStyle, padding, expand);
        }
    }
}
