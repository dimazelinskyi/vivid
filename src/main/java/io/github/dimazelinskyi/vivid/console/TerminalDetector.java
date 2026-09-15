package io.github.dimazelinskyi.vivid.console;

import io.github.dimazelinskyi.vivid.style.Color;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Works out terminal capabilities from environment variables and whether output is interactive.
 * Kept free of global state so it can be tested with any environment.
 */
final class TerminalDetector {

    static final int DEFAULT_WIDTH = 80;

    /** {@code java.io.Console.isTerminal()}, available from JDK 22. */
    private static final MethodHandle IS_TERMINAL = findIsTerminal();

    private final Map<String, String> environment;
    private final boolean interactive;

    TerminalDetector(Map<String, String> environment, boolean interactive) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.interactive = interactive;
    }

    /**
     * Tells whether {@code System.out} is attached to a terminal.
     *
     * <p>Up to JDK 21 {@code System.console()} is {@code null} unless both stdin and stdout are
     * terminals. From JDK 22 it is non-null even when redirected, so {@code isTerminal()} is asked too.
     */
    static boolean systemOutIsTerminal() {
        java.io.Console console = System.console();
        if (console == null) {
            return false;
        }
        if (IS_TERMINAL == null) {
            return true;
        }
        try {
            return (boolean) IS_TERMINAL.invokeExact(console);
        } catch (Throwable e) {
            return false;
        }
    }

    boolean isTerminal() {
        return interactive;
    }

    /**
     * Returns {@code COLUMNS} if it holds a positive number, otherwise {@value #DEFAULT_WIDTH}.
     */
    int width() {
        String columns = environment.get("COLUMNS");
        if (columns != null) {
            try {
                int width = Integer.parseInt(columns.trim());
                if (width > 0) {
                    return width;
                }
            } catch (NumberFormatException ignored) {
                // fall back to the default
            }
        }
        return DEFAULT_WIDTH;
    }

    /**
     * Detects the color depth. {@code NO_COLOR} always wins; {@code FORCE_COLOR} enables color
     * even when output is not a terminal ({@code 0} or {@code false} disables it, {@code 2} and
     * {@code 3} request 256 and true color).
     */
    Color.Depth colorDepth() {
        if (!environment.getOrDefault("NO_COLOR", "").isEmpty()) {
            return Color.Depth.NONE;
        }

        Color.Depth minimum = Color.Depth.NONE;
        String force = environment.get("FORCE_COLOR");
        if (force != null) {
            switch (force.trim().toLowerCase(Locale.ROOT)) {
                case "0", "false" -> {
                    return Color.Depth.NONE;
                }
                case "2" -> minimum = Color.Depth.EIGHT_BIT;
                case "3" -> minimum = Color.Depth.TRUE_COLOR;
                default -> minimum = Color.Depth.STANDARD;
            }
        }

        String term = environment.getOrDefault("TERM", "").toLowerCase(Locale.ROOT);
        if (minimum == Color.Depth.NONE && (!interactive || term.equals("dumb"))) {
            return Color.Depth.NONE;
        }

        Color.Depth detected = detectFromTerminalType(term);
        return detected.compareTo(minimum) >= 0 ? detected : minimum;
    }

    private Color.Depth detectFromTerminalType(String term) {
        String colorTerm = environment.getOrDefault("COLORTERM", "").toLowerCase(Locale.ROOT);
        if (colorTerm.equals("truecolor") || colorTerm.equals("24bit") || environment.containsKey("WT_SESSION")) {
            return Color.Depth.TRUE_COLOR;
        }
        if (term.contains("256color")) {
            return Color.Depth.EIGHT_BIT;
        }
        return Color.Depth.STANDARD;
    }

    private static MethodHandle findIsTerminal() {
        try {
            return MethodHandles.publicLookup()
                    .findVirtual(java.io.Console.class, "isTerminal", MethodType.methodType(boolean.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }
}
