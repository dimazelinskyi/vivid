package io.github.dimazelinskyi.vivid.progress;

/**
 * A live status line: an animated {@link Spinner} next to a message, shown while work is in progress.
 *
 * <p>Obtained from {@link io.github.dimazelinskyi.vivid.console.Console#status(String)}. The spinner animates on a
 * background thread until the status is stopped, after which the line is cleared.
 *
 * <pre>{@code
 * try (Status status = console.status("Connecting to database...")) {
 *     connect();
 *     status.update("Running migrations...");
 *     migrate();
 * }
 * console.success("Database ready");
 * }</pre>
 */
public interface Status extends AutoCloseable {

    /**
     * Replaces the message shown next to the spinner.
     *
     * @param message the new message, interpreted as markup
     */
    void update(String message);

    /** Stops the animation and clears the status line. Calling this more than once has no effect. */
    void stop();

    /** Equivalent to {@link #stop()}, so a status can be used in try-with-resources. */
    @Override
    default void close() {
        stop();
    }
}
