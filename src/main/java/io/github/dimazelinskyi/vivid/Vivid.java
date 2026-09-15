package io.github.dimazelinskyi.vivid;

import io.github.dimazelinskyi.vivid.console.Console;
import io.github.dimazelinskyi.vivid.console.Live;
import io.github.dimazelinskyi.vivid.panel.Panel;
import io.github.dimazelinskyi.vivid.progress.Progress;
import io.github.dimazelinskyi.vivid.progress.ProgressBar;
import io.github.dimazelinskyi.vivid.progress.Spinner;
import io.github.dimazelinskyi.vivid.progress.Status;
import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.style.StyleBuilder;
import io.github.dimazelinskyi.vivid.table.Table;
import io.github.dimazelinskyi.vivid.text.Text;

/**
 * The main entry point to Vivid: a static facade over the most common operations.
 *
 * <p>Everything here is also available through the underlying types ({@link Console},
 * {@link Table}, {@link Panel}, ...). The facade exists so that the common case needs
 * just one import:
 *
 * <pre>{@code
 * Vivid.println("[bold green]Build succeeded[/] in 4.2s");
 *
 * Vivid.println(Vivid.table("Module", "Tests", "Time")
 *         .row("core", "128", "1.9s")
 *         .row("cli", "42", "0.7s")
 *         .build());
 *
 * Vivid.success("Released v1.0.0");
 * }</pre>
 *
 * <p>All output goes to a shared default {@link Console} on {@code System.out}. Create your own
 * with {@link Console#builder()} when you need a different stream, width or color depth.
 */
public final class Vivid {

    private Vivid() {
    }

    // ---------------------------------------------------------------- console

    /**
     * Returns the shared default console, writing to {@code System.out}.
     *
     * @return the default console
     */
    public static Console console() {
        return DefaultConsole.INSTANCE;
    }

    /**
     * Prints to the default console without a trailing newline.
     * Strings are interpreted as markup; {@link Renderable}s are rendered.
     *
     * @param objects the objects to print
     */
    public static void print(Object... objects) {
        console().print(objects);
    }

    /**
     * Prints to the default console followed by a newline.
     * Strings are interpreted as markup; {@link Renderable}s are rendered.
     *
     * @param objects the objects to print
     */
    public static void println(Object... objects) {
        console().println(objects);
    }

    /**
     * Prints a horizontal rule with a centered title to the default console.
     *
     * @param title the title, interpreted as markup
     */
    public static void rule(String title) {
        console().rule(title);
    }

    // ---------------------------------------------------------------- status messages

    /**
     * Prints an informational message to the default console.
     *
     * @param message the message, interpreted as markup
     */
    public static void info(String message) {
        console().info(message);
    }

    /**
     * Prints a success message to the default console.
     *
     * @param message the message, interpreted as markup
     */
    public static void success(String message) {
        console().success(message);
    }

    /**
     * Prints a warning message to the default console.
     *
     * @param message the message, interpreted as markup
     */
    public static void warning(String message) {
        console().warning(message);
    }

    /**
     * Prints an error message to the default console.
     *
     * @param message the message, interpreted as markup
     */
    public static void error(String message) {
        console().error(message);
    }

    // ---------------------------------------------------------------- text and style

    /**
     * Creates plain, unstyled text.
     *
     * @param plain the text, taken literally
     * @return the text
     */
    public static Text text(String plain) {
        return Text.of(plain);
    }

    /**
     * Creates styled text from markup such as {@code "[bold red]Error[/]"}.
     *
     * @param markup the markup
     * @return the styled text
     */
    public static Text markup(String markup) {
        return Text.markup(markup);
    }

    /**
     * Starts building a {@link Style}.
     *
     * @return a fresh style builder
     */
    public static StyleBuilder style() {
        return Style.builder();
    }

    // ---------------------------------------------------------------- layout

    /**
     * Starts building a {@link Table}.
     *
     * @return a fresh table builder
     */
    public static Table.Builder table() {
        return Table.builder();
    }

    /**
     * Starts building a {@link Table} with the given column headers.
     *
     * @param headers the column headers
     * @return a table builder with those columns
     */
    public static Table.Builder table(String... headers) {
        return Table.builder().columns(headers);
    }

    /**
     * Starts building a {@link Panel} around the given content.
     *
     * @param content the content; strings are interpreted as markup
     * @return a fresh panel builder
     */
    public static Panel.Builder panel(Object content) {
        return Panel.builder(content);
    }

    // ---------------------------------------------------------------- progress and live output

    /**
     * Creates a static progress bar snapshot.
     *
     * @param total the amount of work representing 100%
     * @return the progress bar
     */
    public static ProgressBar progressBar(long total) {
        return ProgressBar.of(total);
    }

    /**
     * Creates a live progress display on the default console. Call {@link Progress#start()} to show it.
     *
     * @return the progress display
     */
    public static Progress progress() {
        return Progress.builder().console(console()).build();
    }

    /**
     * Iterates over {@code items} while showing a progress bar on the default console.
     *
     * <pre>{@code
     * for (Path file : Vivid.track(files, "Uploading")) {
     *     upload(file);
     * }
     * }</pre>
     *
     * @param items       the items to iterate
     * @param description text shown next to the bar
     * @param <T>         the element type
     * @return an iterable that reports progress as it is consumed
     */
    public static <T> Iterable<T> track(Iterable<T> items, String description) {
        return progress().track(items, description);
    }

    /**
     * Creates a spinner snapshot using the default animation.
     *
     * @return the spinner
     */
    public static Spinner spinner() {
        return Spinner.of(Spinner.Type.DOTS);
    }

    /**
     * Shows an animated status line on the default console until the returned handle is closed.
     *
     * @param message the message, interpreted as markup
     * @return a handle for updating and stopping the status
     */
    public static Status status(String message) {
        return console().status(message);
    }

    /**
     * Starts a live, in-place display on the default console.
     *
     * @param renderable the initial content
     * @return a handle for updating and stopping the display
     */
    public static Live live(Renderable renderable) {
        return console().live(renderable);
    }

    /** Lazily creates the default console on first use. */
    private static final class DefaultConsole {
        static final Console INSTANCE = Console.create();
    }
}
