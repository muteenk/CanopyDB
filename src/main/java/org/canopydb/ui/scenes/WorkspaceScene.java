package org.canopydb.ui.scenes;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.interfaces.Scene;
import org.canopydb.ui.organisms.workspace.Sidebar;
import org.canopydb.ui.organisms.workspace.Workspace;

public class WorkspaceScene implements Scene {
    private final BorderPane app = new BorderPane();

    public WorkspaceScene() {
        Workspace workspace = new Workspace();
        Sidebar sidebar = new Sidebar(
                workspace::addNewSession,
                workspace::isSessionActive
        );
        app.setLeft(sidebar.getSidebar());
        app.setCenter(workspace.getWorkspace());
    }

    public Parent getScene() {return this.app;}
}
