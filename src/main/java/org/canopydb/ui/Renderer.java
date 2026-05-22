package org.canopydb.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.canopydb.ui.molecules.Notification;
import org.canopydb.ui.organisms.Workspace;
import org.canopydb.ui.organisms.Sidebar;

import java.util.Objects;

public class Renderer {
    private final StackPane root = new StackPane();
    private final Notification notification = new Notification();

    private Parent build() {
        BorderPane app = new BorderPane();
        root.getChildren().addAll(
                app,
                notification.getContainer()
        );
        StackPane.setAlignment(
                notification.getContainer(),
                Pos.BOTTOM_RIGHT
        );

        Workspace workspace = new Workspace(
                notification::pushNotification
        );
        Sidebar sidebar = new Sidebar(
                workspace::addTable,
                workspace::isTableOpen,
                notification::pushNotification
        );
        app.setLeft(sidebar.getSidebar());
        app.setCenter(workspace.getWorkspace());

        return root;
    }

    public Scene render() {
        Scene scene = new Scene(build(), 1280, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/style.css")
                ).toExternalForm()
        );
        return scene;
    }
}
