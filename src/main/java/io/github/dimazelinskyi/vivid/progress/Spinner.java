package io.github.dimazelinskyi.vivid.progress;

import io.github.dimazelinskyi.vivid.render.Renderable;
import io.github.dimazelinskyi.vivid.style.Style;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * An immutable spinner definition: an animation, optional trailing text and a style.
 *
 * <p>Like {@link ProgressBar}, a spinner is a {@link Renderable} snapshot; it picks the frame to
 * draw from the elapsed time. For a spinner that animates on its own, use
 * {@link io.github.dimazelinskyi.vivid.console.Console#status(String)}.
 *
 * <pre>{@code
 * Spinner spinner = Spinner.of(Spinner.Type.DOTS)
 *         .withText("Resolving dependencies...")
 *         .withStyle(Style.of(Color.CYAN));
 * }</pre>
 *
 * @param type  the animation
 * @param text  text shown after the spinner, interpreted as markup; empty for none
 * @param style style applied to the spinner frames
 */
public record Spinner(Type type, String text, Style style) implements Renderable {

    public Spinner {
        Objects.requireNonNull(type, "type");
        text = text == null ? "" : text;
        style = style == null ? Style.NONE : style;
    }

    /**
     * Creates a spinner with the given animation and no text.
     *
     * @param type the animation
     * @return the spinner
     */
    public static Spinner of(Type type) {
        return new Spinner(type, "", Style.NONE);
    }

    /**
     * Returns a copy with different trailing text.
     *
     * @param newText the text, interpreted as markup
     * @return the new spinner
     */
    public Spinner withText(String newText) {
        return new Spinner(type, newText, style);
    }

    /**
     * Returns a copy with a different frame style.
     *
     * @param newStyle the style
     * @return the new spinner
     */
    public Spinner withStyle(Style newStyle) {
        return new Spinner(type, text, newStyle);
    }

    /**
     * Returns the animation frame to show after the given time has elapsed.
     *
     * @param elapsed the time since the spinner started
     * @return the frame
     */
    public String frameAt(Duration elapsed) {
        List<String> frames = type.frames();
        long tick = elapsed.toMillis() / type.interval().toMillis();
        return frames.get((int) (tick % frames.size()));
    }

    @Override
    public List<String> render(Renderable.Context context) {
        throw new UnsupportedOperationException("Spinner rendering is not implemented yet");
    }

    /** Built-in spinner animations. */
    public enum Type {

        /** Braille dots. Smooth and compact; the default. */
        DOTS(80, "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"),

        /** A rotating line. Works in any terminal. */
        LINE(130, "-", "\\", "|", "/"),

        /** A rotating arc. */
        ARC(100, "◜", "◠", "◝", "◞", "◡", "◟"),

        /** A rotating half-filled circle. */
        CIRCLE(120, "◐", "◓", "◑", "◒"),

        /** Growing ellipsis dots. */
        ELLIPSIS(300, ".  ", ".. ", "...", "   ");

        private final Duration interval;
        private final List<String> frames;

        Type(long intervalMillis, String... frames) {
            this.interval = Duration.ofMillis(intervalMillis);
            this.frames = List.of(frames);
        }

        /**
         * Returns how long each frame is shown.
         *
         * @return the frame interval
         */
        public Duration interval() {
            return interval;
        }

        /**
         * Returns the animation frames, in order.
         *
         * @return the frames
         */
        public List<String> frames() {
            return frames;
        }
    }
}
