package io.github.dimazelinskyi.vivid.progress;

import io.github.dimazelinskyi.vivid.console.Console;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * A live display of one or more progress bars that redraws in place while work runs.
 *
 * <p>Each unit of work is a {@link Task}. Tasks may be advanced from any thread; the display
 * refreshes on its own schedule, so updating a task is cheap.
 *
 * <pre>{@code
 * try (Progress progress = Progress.builder().build().start()) {
 *     Progress.Task download = progress.addTask("Downloading", 1024);
 *     Progress.Task extract  = progress.addTask("Extracting", 300);
 *
 *     while (!download.isFinished()) {
 *         download.advance(readChunk());
 *     }
 * }
 * }</pre>
 *
 * <p>For the common case of iterating over a collection, see {@link #track(Iterable, String)}.
 */
public final class Progress implements AutoCloseable {

    private final Console console;
    private final Duration refreshInterval;
    private final boolean clearOnExit;

    private Progress(Builder builder) {
        this.console = builder.console != null ? builder.console : Console.create();
        this.refreshInterval = builder.refreshInterval;
        this.clearOnExit = builder.clearOnExit;
    }

    /**
     * Starts configuring a new progress display.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the console this display draws to.
     *
     * @return the console
     */
    public Console console() {
        return console;
    }

    /**
     * Starts drawing and refreshing the display.
     *
     * @return this progress, for chaining
     */
    public Progress start() {
        throw new UnsupportedOperationException("Live progress is not implemented yet");
    }

    /**
     * Stops refreshing and leaves the final state on screen, or clears it if configured to.
     */
    public void stop() {
        throw new UnsupportedOperationException("Live progress is not implemented yet");
    }

    /** Equivalent to {@link #stop()}, so the display can be used in try-with-resources. */
    @Override
    public void close() {
        stop();
    }

    /**
     * Adds a new task with a known total.
     *
     * @param description text shown next to the bar, interpreted as markup
     * @param total       the amount of work representing 100%, or {@code 0} if unknown
     * @return a handle for updating the task
     */
    public Task addTask(String description, long total) {
        throw new UnsupportedOperationException("Live progress is not implemented yet");
    }

    /**
     * Returns all tasks added so far, in display order.
     *
     * @return the tasks
     */
    public List<Task> tasks() {
        throw new UnsupportedOperationException("Live progress is not implemented yet");
    }

    /**
     * Tells whether every task has finished.
     *
     * @return {@code true} if all tasks are finished
     */
    public boolean isFinished() {
        return tasks().stream().allMatch(Task::isFinished);
    }

    /**
     * Wraps an iterable so that iterating over it advances a new task by one per element.
     * The total is taken from the collection size when available.
     *
     * <pre>{@code
     * for (Path file : progress.track(files, "Compressing")) {
     *     compress(file);
     * }
     * }</pre>
     *
     * @param items       the items to iterate
     * @param description text shown next to the bar
     * @param <T>         the element type
     * @return an iterable that reports progress as it is consumed
     */
    public <T> Iterable<T> track(Iterable<T> items, String description) {
        throw new UnsupportedOperationException("Live progress is not implemented yet");
    }

    /**
     * A handle to a single task within a {@link Progress} display. Implementations are thread-safe.
     */
    public interface Task {

        /**
         * Returns the text shown next to the bar.
         *
         * @return the description
         */
        String description();

        /**
         * Returns the amount of work representing 100%.
         *
         * @return the total, or {@code 0} if unknown
         */
        long total();

        /**
         * Returns the amount of work done so far.
         *
         * @return the completed amount
         */
        long completed();

        /**
         * Adds to the completed amount.
         *
         * @param amount the additional work done
         */
        void advance(long amount);

        /**
         * Sets the completed amount.
         *
         * @param completed the total work done
         */
        void update(long completed);

        /**
         * Replaces the text shown next to the bar.
         *
         * @param description the new description, interpreted as markup
         */
        void describe(String description);

        /**
         * Tells whether all work for this task is done.
         *
         * @return {@code true} if finished
         */
        boolean isFinished();
    }

    /**
     * A fluent builder for {@link Progress}. Mutable and not thread-safe.
     */
    public static final class Builder {

        private Console console;
        private Duration refreshInterval = Duration.ofMillis(100);
        private boolean clearOnExit;

        private Builder() {
        }

        /**
         * Sets the console to draw to. Defaults to a new console on {@code System.out}.
         *
         * @param console the console
         * @return this builder
         */
        public Builder console(Console console) {
            this.console = Objects.requireNonNull(console, "console");
            return this;
        }

        /**
         * Sets how often the display is redrawn. Defaults to 100 ms.
         *
         * @param refreshInterval the redraw interval
         * @return this builder
         */
        public Builder refreshInterval(Duration refreshInterval) {
            this.refreshInterval = Objects.requireNonNull(refreshInterval, "refreshInterval");
            return this;
        }

        /**
         * Sets whether the display is erased when stopped. Defaults to {@code false}.
         *
         * @param clearOnExit {@code true} to leave no trace once done
         * @return this builder
         */
        public Builder clearOnExit(boolean clearOnExit) {
            this.clearOnExit = clearOnExit;
            return this;
        }

        /**
         * Creates the progress display. It does not draw anything until {@link Progress#start()}.
         *
         * @return the progress display
         */
        public Progress build() {
            return new Progress(this);
        }
    }
}
