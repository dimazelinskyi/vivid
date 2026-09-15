package io.github.dimazelinskyi.vivid.console;

import io.github.dimazelinskyi.vivid.progress.Status;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;

import java.io.PrintStream;
import java.util.Objects;

/**
 * The output destination for everything Vivid draws.
 *
 * <p>A console knows the capabilities of the terminal behind its stream — width, color depth,
 * whether it is interactive — and adapts output accordingly: colors are downgraded or dropped,
 * content is wrapped to fit, and animations are disabled when output is redirected to a file.
 *
 * <pre>{@code
 * Console console = Console.create();
 *
 * console.println("[bold]Hello[/], [green]world[/]!")
 *        .rule("Summary")
 *        .println(table);
 *
 * console.success("All 42 tests passed");
 * }</pre>
 *
 * <p>Use {@link #builder()} to write to a different stream or override detected capabilities,
 * for example in tests:
 *
 * <pre>{@code
 * Console console = Console.builder()
 *         .output(new PrintStream(buffer, true, UTF_8))
 *         .width(80)
 *         .noColor()
 *         .build();
 * }</pre>
 *
 * <p>Consoles are thread-safe: concurrent prints are never interleaved mid-line.
 */
public final class Console {

    private final PrintStream out;
    private final int width;
    private final Color.Depth colorDepth;
    private final boolean forceTerminal;

    private Console(Builder builder) {
        this.out = builder.out;
        this.width = builder.width;
        this.colorDepth = builder.colorDepth;
        this.forceTerminal = builder.forceTerminal;
    }

    /**
     * Creates a console on {@code System.out} with auto-detected capabilities.
     *
     * @return the console
     */
    public static Console create() {
        return builder().build();
    }

    /**
     * Starts configuring a new console.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Prints objects separated by spaces, without a trailing newline.
     * Strings are interpreted as markup; {@link Renderable}s are rendered.
     *
     * @param objects the objects to print
     * @return this console
     */
    public Console print(Object... objects) {
        throw new UnsupportedOperationException("Printing is not implemented yet");
    }

    /**
     * Prints objects separated by spaces, followed by a newline.
     * Strings are interpreted as markup; {@link Renderable}s are rendered.
     *
     * @param objects the objects to print
     * @return this console
     */
    public Console println(Object... objects) {
        throw new UnsupportedOperationException("Printing is not implemented yet");
    }

    /**
     * Prints an empty line.
     *
     * @return this console
     */
    public Console newLine() {
        return println();
    }

    /**
     * Prints a horizontal rule across the full width of the console.
     *
     * @return this console
     */
    public Console rule() {
        throw new UnsupportedOperationException("Rules are not implemented yet");
    }

    /**
     * Prints a horizontal rule with a centered title: {@code ──── Title ────}.
     *
     * @param title the title, interpreted as markup
     * @return this console
     */
    public Console rule(String title) {
        throw new UnsupportedOperationException("Rules are not implemented yet");
    }

    /**
     * Prints an informational message with a blue {@code ℹ} prefix.
     *
     * @param message the message, interpreted as markup
     * @return this console
     */
    public Console info(String message) {
        throw new UnsupportedOperationException("Status messages are not implemented yet");
    }

    /**
     * Prints a success message with a green {@code ✔} prefix.
     *
     * @param message the message, interpreted as markup
     * @return this console
     */
    public Console success(String message) {
        throw new UnsupportedOperationException("Status messages are not implemented yet");
    }

    /**
     * Prints a warning message with a yellow {@code ⚠} prefix.
     *
     * @param message the message, interpreted as markup
     * @return this console
     */
    public Console warning(String message) {
        throw new UnsupportedOperationException("Status messages are not implemented yet");
    }

    /**
     * Prints an error message with a red {@code ✖} prefix.
     *
     * @param message the message, interpreted as markup
     * @return this console
     */
    public Console error(String message) {
        throw new UnsupportedOperationException("Status messages are not implemented yet");
    }

    /**
     * Shows an animated spinner with a message until the returned {@link Status} is closed.
     *
     * @param message the message, interpreted as markup
     * @return a handle for updating and stopping the status
     */
    public Status status(String message) {
        throw new UnsupportedOperationException("Status spinners are not implemented yet");
    }

    /**
     * Starts a live display that redraws the given content in place until closed.
     *
     * @param renderable the initial content
     * @return a handle for updating and stopping the display
     */
    public Live live(Renderable renderable) {
        throw new UnsupportedOperationException("Live display is not implemented yet");
    }

    /**
     * Returns the stream this console writes to.
     *
     * @return the output stream
     */
    public PrintStream out() {
        return out;
    }

    /**
     * Returns the width of the console in terminal cells: the configured width if set,
     * otherwise the detected terminal width.
     *
     * @return the width in cells
     */
    public int width() {
        if (width > 0) {
            return width;
        }
        throw new UnsupportedOperationException("Terminal width detection is not implemented yet");
    }

    /**
     * Returns the richest color encoding this console will emit: the configured depth if set,
     * otherwise the depth detected from the environment ({@code COLORTERM}, {@code TERM},
     * {@code NO_COLOR}, ...).
     *
     * @return the color depth
     */
    public Color.Depth colorDepth() {
        if (colorDepth != null) {
            return colorDepth;
        }
        throw new UnsupportedOperationException("Color depth detection is not implemented yet");
    }

    /**
     * Tells whether output goes to an interactive terminal. Animations and live displays are
     * only drawn when this is {@code true}.
     *
     * @return {@code true} if writing to a terminal
     */
    public boolean isTerminal() {
        if (forceTerminal) {
            return true;
        }
        throw new UnsupportedOperationException("Terminal detection is not implemented yet");
    }

    /**
     * A fluent builder for {@link Console}. Anything not configured is detected from the environment.
     */
    public static final class Builder {

        private PrintStream out = System.out;
        private int width;
        private Color.Depth colorDepth;
        private boolean forceTerminal;

        private Builder() {
        }

        /**
         * Sets the stream to write to. Defaults to {@code System.out}.
         *
         * @param out the output stream
         * @return this builder
         */
        public Builder output(PrintStream out) {
            this.out = Objects.requireNonNull(out, "out");
            return this;
        }

        /**
         * Fixes the console width instead of detecting it.
         *
         * @param width the width in terminal cells
         * @return this builder
         */
        public Builder width(int width) {
            if (width <= 0) {
                throw new IllegalArgumentException("width must be positive, got " + width);
            }
            this.width = width;
            return this;
        }

        /**
         * Fixes the color depth instead of detecting it.
         *
         * @param colorDepth the color depth
         * @return this builder
         */
        public Builder colorDepth(Color.Depth colorDepth) {
            this.colorDepth = Objects.requireNonNull(colorDepth, "colorDepth");
            return this;
        }

        /**
         * Disables all color output. Shorthand for {@code colorDepth(Color.Depth.NONE)}.
         *
         * @return this builder
         */
        public Builder noColor() {
            return colorDepth(Color.Depth.NONE);
        }

        /**
         * Treats the output as an interactive terminal even when it is not, e.g. to keep
         * animations in CI logs that support ANSI.
         *
         * @param forceTerminal {@code true} to force terminal behavior
         * @return this builder
         */
        public Builder forceTerminal(boolean forceTerminal) {
            this.forceTerminal = forceTerminal;
            return this;
        }

        /**
         * Creates the console.
         *
         * @return the console
         */
        public Console build() {
            return new Console(this);
        }
    }
}
