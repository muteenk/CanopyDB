package org.canopydb.ui.atoms;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

/**
 * Compact square icon buttons used in toolbars.
 */
public final class IconButton {

    private static final double ICON_SCALE = 0.58;

    private IconButton() {
    }

    public static Button refresh(String tooltip) {
        return create(
                tooltip,
                icon("M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z")
        );
    }

    public static Button plus(String tooltip) {
        return create(
                tooltip,
                icon("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
        );
    }

    private static Button create(String tooltip, Node graphic) {
        Button button = new Button();
        button.setGraphic(graphic);
        button.getStyleClass().add("sidebar-icon-button");
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    private static Node icon(String pathContent) {
        SVGPath path = new SVGPath();
        path.setContent(pathContent);
        path.getStyleClass().add("sidebar-icon-graphic");

        Group group = new Group(path);
        group.setScaleX(ICON_SCALE);
        group.setScaleY(ICON_SCALE);
        return group;
    }
}
