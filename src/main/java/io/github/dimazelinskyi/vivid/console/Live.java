package io.github.dimazelinskyi.vivid.console;

import io.github.dimazelinskyi.vivid.render.Renderable;

/**
 * A region of the terminal that is redrawn in place, for dashboards and other live displays.
 *
 * <p>Obtained from {@link Console#live(Renderable)}. While a live display is active, other output
 * printed to the same console appears above it rather than corrupting it.
 *
 * <pre>{@code
 * try (Live live = console.live(buildTable())) {
 *     while (running) {
 *         live.update(buildTable());
 *         Thread.sleep(250);
 *     }
 * }
 * }</pre>
 */
public interface Live extends AutoCloseable {

    /**
     * Replaces the displayed content and redraws.
     *
     * @param renderable the new content
     */
    void update(Renderable renderable);

    /** Redraws the current content, for renderables whose output changes over time. */
    void refresh();

    /** Stops redrawing, leaving the last frame on screen. Calling this more than once has no effect. */
    void stop();

    /** Equivalent to {@link #stop()}, so a live display can be used in try-with-resources. */
    @Override
    default void close() {
        stop();
    }
}
