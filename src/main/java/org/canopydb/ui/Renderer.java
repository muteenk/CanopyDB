package org.canopydb.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.organisms.Workspace;
import org.canopydb.ui.organisms.Sidebar;

import java.util.Objects;

public class Renderer {
    private Parent build() {
        Workspace workspace = new Workspace();
        Sidebar sidebar = new Sidebar(workspace::addTable, workspace::isTableOpen);
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(sidebar.getSidebar());
        borderPane.setCenter(workspace.getWorkspace());

        return borderPane;
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
