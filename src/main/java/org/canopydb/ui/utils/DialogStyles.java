package org.canopydb.ui.utils;

import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

/**
 * Applies the app's global stylesheets to a {@link DialogPane}.
 */
public final class DialogStyles {

    private DialogStyles() {
    }

    public static void apply(Dialog<?> dialog, Scene scene) {
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("canopy-dialog");
        pane.setHeader(null);
        pane.setMinWidth(340);
        if (scene != null) {
            pane.getStylesheets().addAll(scene.getStylesheets());
        }
    }
}
