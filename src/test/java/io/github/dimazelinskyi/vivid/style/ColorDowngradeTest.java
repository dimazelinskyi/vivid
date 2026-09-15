package io.github.dimazelinskyi.vivid.style;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ColorDowngradeTest {

    @Test
    void trueColorKeepsEveryColor() {
        Color rgb = Color.rgb(1, 2, 3);

        assertSame(rgb, rgb.downgrade(Color.Depth.TRUE_COLOR));
        assertSame(Color.RED, Color.RED.downgrade(Color.Depth.TRUE_COLOR));
    }

    @Test
    void noneDropsColor() {
        assertNull(Color.rgb(255, 0, 0).downgrade(Color.Depth.NONE));
        assertNull(Color.RED.downgrade(Color.Depth.NONE));
    }

    @Nested
    class EightBit {

        @Test
        void rgbMapsToNearestCubeColor() {
            assertEquals(Color.indexed(208), Color.rgb(255, 136, 0).downgrade(Color.Depth.EIGHT_BIT));
            assertEquals(Color.indexed(196), Color.rgb(250, 3, 3).downgrade(Color.Depth.EIGHT_BIT));
        }

        @Test
        void grayRgbPrefersGrayscaleRamp() {
            assertEquals(Color.indexed(244), Color.rgb(128, 128, 128).downgrade(Color.Depth.EIGHT_BIT));
        }

        @Test
        void blackStaysInCube() {
            assertEquals(Color.indexed(16), Color.rgb(0, 0, 0).downgrade(Color.Depth.EIGHT_BIT));
        }

        @Test
        void paletteColorsAreUnchanged() {
            assertEquals(Color.indexed(42), Color.indexed(42).downgrade(Color.Depth.EIGHT_BIT));
            assertSame(Color.BLUE, Color.BLUE.downgrade(Color.Depth.EIGHT_BIT));
        }
    }

    @Nested
    class Standard {

        @Test
        void lowPaletteIndicesAreTheStandardColors() {
            assertEquals(Color.Standard.BRIGHT_RED, Color.indexed(9).downgrade(Color.Depth.STANDARD));
        }

        @Test
        void paletteColorsMapToNearestStandard() {
            assertEquals(Color.Standard.BRIGHT_RED, Color.indexed(196).downgrade(Color.Depth.STANDARD));
        }

        @Test
        void rgbMapsToNearestStandard() {
            assertEquals(Color.Standard.BRIGHT_RED, Color.rgb(250, 5, 5).downgrade(Color.Depth.STANDARD));
            assertEquals(Color.RED, Color.rgb(200, 0, 0).downgrade(Color.Depth.STANDARD));
            assertEquals(Color.Standard.BRIGHT_BLACK, Color.rgb(128, 128, 128).downgrade(Color.Depth.STANDARD));
        }

        @Test
        void standardColorsAreUnchanged() {
            assertSame(Color.GREEN, Color.GREEN.downgrade(Color.Depth.STANDARD));
        }
    }
}
