package io.github.dimazelinskyi.vivid.render;

/**
 * Horizontal alignment of content within the space available to it.
 *
 * <p>Used by {@link io.github.dimazelinskyi.vivid.text.Text}, table {@link io.github.dimazelinskyi.vivid.table.Column columns}
 * and {@link io.github.dimazelinskyi.vivid.panel.Panel panel} titles.
 */
public enum Justify {

    /** Align content to the left edge. */
    LEFT,

    /** Center content, biasing any odd leftover cell to the right. */
    CENTER,

    /** Align content to the right edge. */
    RIGHT
}
