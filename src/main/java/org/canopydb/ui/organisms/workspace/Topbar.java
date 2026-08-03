package org.canopydb.ui.organisms.workspace;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.ui.singletons.LoadingManager;

/**
 * Workspace chrome above the sidebar + tabs.
 * Hosts the global loading strip from {@link LoadingManager}.
 */
public class Topbar {

    private final VBox root = new VBox();
    private final HBox toolbar = new HBox();

    public Topbar() {
        toolbar.getStyleClass().add("topbar-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setFillHeight(true);

        ProgressBar progressBar = LoadingManager.getNode();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(progressBar, Priority.NEVER);

        root.getStyleClass().add("topbar");
        root.getChildren().addAll(toolbar, progressBar);
        root.setFillWidth(true);
    }

    public VBox getTopbar() {
        return root;
    }
}
