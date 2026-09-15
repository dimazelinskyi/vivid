package io.github.dimazelinskyi.vivid.render;

import java.util.Objects;

/**
 * Helpers for rendered lines: strings that may contain ANSI SGR escape sequences.
 *
 * <p>Widths count visible characters only; escape sequences take no space. Like the rest of
 * Vivid for now, one UTF-16 character is assumed to occupy one terminal cell.
 */
public final class Lines {

    /** The character that marks truncated content. */
    public static final char ELLIPSIS = '…';

    private Lines() {
    }

    /**
     * Returns the number of visible characters in a rendered line.
     *
     * @param line a rendered line
     * @return the width in cells
     */
    public static int width(String line) {
        return Ansi.strip(line).length();
    }

    /**
     * Returns a line that is exactly {@code width} cells wide: shorter lines are padded with
     * trailing spaces, longer ones are {@link #truncate(String, int) truncated}.
     *
     * @param line  a rendered line
     * @param width the target width in cells
     * @return the fitted line
     */
    public static String fit(String line, int width) {
        int visible = width(line);
        return visible <= width ? line + " ".repeat(Math.max(0, width - visible)) : truncate(line, width);
    }

    /**
     * Shortens a line to at most {@code width} cells, replacing the cut-off part with {@link #ELLIPSIS}.
     * Escape sequences are never split, and the result ends with a reset if it was cut inside a style.
     *
     * @param line  a rendered line
     * @param width the maximum width in cells
     * @return the line, unchanged if it already fits
     */
    public static String truncate(String line, int width) {
        Objects.requireNonNull(line, "line");
        if (width(line) <= width) {
            return line;
        }
        if (width <= 0) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int shown = 0;
        boolean styled = false;
        int i = 0;
        while (i < line.length()) {
            int end = sgrEnd(line, i);
            if (end > 0) {
                String sequence = line.substring(i, end);
                out.append(sequence);
                styled = !sequence.equals(Ansi.RESET);
                i = end;
                continue;
            }
            if (shown == width - 1) {
                break;
            }
            out.append(line.charAt(i));
            shown++;
            i++;
        }
        out.append(ELLIPSIS);
        if (styled) {
            out.append(Ansi.RESET);
        }
        return out.toString();
    }

    /** Returns the index just past an SGR sequence starting at {@code start}, or -1 if there is none. */
    private static int sgrEnd(String line, int start) {
        if (line.charAt(start) != '' || start + 1 >= line.length() || line.charAt(start + 1) != '[') {
            return -1;
        }
        int j = start + 2;
        while (j < line.length() && (Character.isDigit(line.charAt(j)) || line.charAt(j) == ';')) {
            j++;
        }
        return j < line.length() && line.charAt(j) == 'm' ? j + 1 : -1;
    }
}
