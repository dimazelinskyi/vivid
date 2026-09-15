package io.github.dimazelinskyi.vivid.render;

import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.text.Text;

import java.util.List;
import java.util.Objects;

/**
 * Anything that can be drawn to a terminal.
 *
 * <p>This is the single abstraction every visual element in Vivid shares — {@link Text},
 * tables, panels, progress bars — and the extension point for user-defined components.
 * Given the constraints in a {@link Context}, a renderable produces the lines it occupies,
 * each already containing the ANSI escape sequences for its styling.
 *
 * <pre>{@code
 * Renderable divider = context -> List.of("-".repeat(context.maxWidth()));
 * console.println(divider);
 * }</pre>
 *
 * <p><strong>Note:</strong> the line-based contract is deliberately minimal for now. It is
 * expected to evolve into a richer segment model (text + style pairs plus measurement) before
 * 1.0, so that containers can wrap, crop and restyle their children.
 */
@FunctionalInterface
public interface Renderable {

    /**
     * Renders this element into terminal lines.
     *
     * @param context width and color constraints for this render pass
     * @return the rendered lines, never {@code null}; no line may exceed
     *         {@link Context#maxWidth()} visible cells
     */
    List<String> render(Context context);

    /**
     * Adapts an arbitrary object to a {@link Renderable}.
     *
     * <p>Renderables are returned unchanged. Strings are interpreted as
     * {@link io.github.dimazelinskyi.vivid.text.Markup markup}. Any other value is converted with
     * {@link String#valueOf(Object)} and shown literally, so that e.g. a list printed as {@code [red]}
     * is not mistaken for a tag.
     *
     * @param value the object to adapt
     * @return a renderable for {@code value}
     * @throws IllegalArgumentException if {@code value} is a string with malformed markup
     */
    static Renderable from(Object value) {
        Objects.requireNonNull(value, "value");
        if (value instanceof Renderable renderable) {
            return renderable;
        }
        if (value instanceof String markup) {
            return Text.markup(markup);
        }
        return Text.of(String.valueOf(value));
    }

    /**
     * Constraints for a single render pass.
     *
     * @param maxWidth   the maximum number of terminal cells available per line; must be positive
     * @param colorDepth the richest color encoding the target terminal understands
     */
    record Context(int maxWidth, Color.Depth colorDepth) {

        public Context {
            if (maxWidth <= 0) {
                throw new IllegalArgumentException("maxWidth must be positive, got " + maxWidth);
            }
            Objects.requireNonNull(colorDepth, "colorDepth");
        }

        /**
         * Creates a context with the given width and true-color output.
         *
         * @param maxWidth the maximum number of terminal cells per line
         * @return a new context
         */
        public static Context of(int maxWidth) {
            return new Context(maxWidth, Color.Depth.TRUE_COLOR);
        }
    }
}
