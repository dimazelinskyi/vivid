package io.github.dimazelinskyi.vivid.progress;

import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;

import java.util.List;

/**
 * An immutable snapshot of a single progress bar.
 *
 * <p>A {@code ProgressBar} is a plain {@link Renderable}: it draws the bar for one moment in time.
 * To animate bars in place, use {@link Progress}, which manages live redrawing for you. A bar
 * with a total of {@code 0} is <em>indeterminate</em> and renders as a pulsing animation.
 *
 * <pre>{@code
 * ProgressBar bar = ProgressBar.of(200).withCompleted(50).withWidth(30);
 * bar.percentage();   // 25.0
 * console.println(bar); // ━━━━━━━╺━━━━━━━━━━━━━━━━━━━━━━
 * }</pre>
 *
 * @param total          the amount of work representing 100%, or {@code 0} if unknown
 * @param completed      the amount of work done so far
 * @param width          the bar width in terminal cells
 * @param completeStyle  style of the completed portion while in progress
 * @param remainingStyle style of the remaining portion
 * @param finishedStyle  style of the whole bar once finished
 */
public record ProgressBar(
        long total,
        long completed,
        int width,
        Style completeStyle,
        Style remainingStyle,
        Style finishedStyle) implements Renderable {

    /** The default bar width, in terminal cells. */
    public static final int DEFAULT_WIDTH = 40;

    /** The default style for the completed portion. */
    public static final Style DEFAULT_COMPLETE_STYLE = Style.of(Color.rgb(249, 38, 114));

    /** The default style for the remaining portion. */
    public static final Style DEFAULT_REMAINING_STYLE = Style.of(Color.rgb(58, 58, 58));

    /** The default style for a finished bar. */
    public static final Style DEFAULT_FINISHED_STYLE = Style.of(Color.rgb(114, 156, 31));

    public ProgressBar {
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative, got " + total);
        }
        if (completed < 0) {
            throw new IllegalArgumentException("completed must not be negative, got " + completed);
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive, got " + width);
        }
        completeStyle = completeStyle == null ? DEFAULT_COMPLETE_STYLE : completeStyle;
        remainingStyle = remainingStyle == null ? DEFAULT_REMAINING_STYLE : remainingStyle;
        finishedStyle = finishedStyle == null ? DEFAULT_FINISHED_STYLE : finishedStyle;
    }

    /**
     * Creates an empty bar with default width and styles.
     *
     * @param total the amount of work representing 100%, or {@code 0} if unknown
     * @return the bar
     */
    public static ProgressBar of(long total) {
        return new ProgressBar(total, 0, DEFAULT_WIDTH, null, null, null);
    }

    /**
     * Returns a copy with a different completed amount.
     *
     * @param newCompleted the amount of work done
     * @return the new bar
     */
    public ProgressBar withCompleted(long newCompleted) {
        return new ProgressBar(total, newCompleted, width, completeStyle, remainingStyle, finishedStyle);
    }

    /**
     * Returns a copy advanced by the given amount.
     *
     * @param amount the additional work done
     * @return the new bar
     */
    public ProgressBar advance(long amount) {
        return withCompleted(completed + amount);
    }

    /**
     * Returns a copy with a different width.
     *
     * @param newWidth the width in terminal cells
     * @return the new bar
     */
    public ProgressBar withWidth(int newWidth) {
        return new ProgressBar(total, completed, newWidth, completeStyle, remainingStyle, finishedStyle);
    }

    /**
     * Returns a copy with a different style for the completed portion.
     *
     * @param style the style
     * @return the new bar
     */
    public ProgressBar withCompleteStyle(Style style) {
        return new ProgressBar(total, completed, width, style, remainingStyle, finishedStyle);
    }

    /**
     * Returns a copy with a different style for the remaining portion.
     *
     * @param style the style
     * @return the new bar
     */
    public ProgressBar withRemainingStyle(Style style) {
        return new ProgressBar(total, completed, width, completeStyle, style, finishedStyle);
    }

    /**
     * Returns a copy with a different style for the finished bar.
     *
     * @param style the style
     * @return the new bar
     */
    public ProgressBar withFinishedStyle(Style style) {
        return new ProgressBar(total, completed, width, completeStyle, remainingStyle, style);
    }

    /**
     * Returns the completion percentage, clamped to {@code [0, 100]}.
     *
     * @return the percentage, or {@code 0} for an indeterminate bar
     */
    public double percentage() {
        return isIndeterminate() ? 0 : Math.min(100.0, completed * 100.0 / total);
    }

    /**
     * Tells whether the total is unknown.
     *
     * @return {@code true} if {@link #total()} is {@code 0}
     */
    public boolean isIndeterminate() {
        return total == 0;
    }

    /**
     * Tells whether all work is done.
     *
     * @return {@code true} if the total is known and has been reached
     */
    public boolean isFinished() {
        return !isIndeterminate() && completed >= total;
    }

    @Override
    public List<String> render(Renderable.Context context) {
        throw new UnsupportedOperationException("Progress bar rendering is not implemented yet");
    }
}
