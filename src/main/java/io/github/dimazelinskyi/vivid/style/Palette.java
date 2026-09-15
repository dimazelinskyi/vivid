package io.github.dimazelinskyi.vivid.style;

/**
 * The xterm 256-color palette and nearest-color lookups used by {@link Color#downgrade(Color.Depth)}.
 *
 * <p>Indices 0–15 are the standard colors (xterm defaults; real terminals theme them),
 * 16–231 a 6×6×6 color cube and 232–255 a 24-step grayscale ramp.
 */
final class Palette {

    private static final int[] CUBE_LEVELS = {0, 95, 135, 175, 215, 255};

    private static final int[][] STANDARD = {
        {0, 0, 0}, {205, 0, 0}, {0, 205, 0}, {205, 205, 0},
        {0, 0, 238}, {205, 0, 205}, {0, 205, 205}, {229, 229, 229},
        {127, 127, 127}, {255, 0, 0}, {0, 255, 0}, {255, 255, 0},
        {92, 92, 255}, {255, 0, 255}, {0, 255, 255}, {255, 255, 255}
    };

    private Palette() {
    }

    /**
     * Returns the RGB value of a palette entry.
     *
     * @param index palette index, 0–255
     * @return {@code {red, green, blue}}
     */
    static int[] rgb(int index) {
        if (index < 16) {
            return STANDARD[index].clone();
        }
        if (index < 232) {
            int cube = index - 16;
            return new int[] {CUBE_LEVELS[cube / 36], CUBE_LEVELS[(cube / 6) % 6], CUBE_LEVELS[cube % 6]};
        }
        int gray = 8 + (index - 232) * 10;
        return new int[] {gray, gray, gray};
    }

    /**
     * Finds the closest color in the cube or grayscale ramp (indices 16–255). The theme-dependent
     * standard colors are deliberately skipped.
     */
    static int nearestIndexed(int red, int green, int blue) {
        int cubeIndex = 16 + 36 * nearestLevel(red) + 6 * nearestLevel(green) + nearestLevel(blue);

        int average = (red + green + blue) / 3;
        int grayStep = Math.max(0, Math.min(23, Math.round((average - 8) / 10f)));
        int grayIndex = 232 + grayStep;

        return distance(rgb(grayIndex), red, green, blue) < distance(rgb(cubeIndex), red, green, blue)
                ? grayIndex
                : cubeIndex;
    }

    /** Finds the closest of the 16 standard colors (indices 0–15). */
    static int nearestStandard(int red, int green, int blue) {
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < STANDARD.length; i++) {
            int d = distance(STANDARD[i], red, green, blue);
            if (d < bestDistance) {
                best = i;
                bestDistance = d;
            }
        }
        return best;
    }

    private static int nearestLevel(int channel) {
        int best = 0;
        for (int i = 1; i < CUBE_LEVELS.length; i++) {
            if (Math.abs(CUBE_LEVELS[i] - channel) < Math.abs(CUBE_LEVELS[best] - channel)) {
                best = i;
            }
        }
        return best;
    }

    private static int distance(int[] rgb, int red, int green, int blue) {
        int dr = rgb[0] - red;
        int dg = rgb[1] - green;
        int db = rgb[2] - blue;
        return dr * dr + dg * dg + db * db;
    }
}
