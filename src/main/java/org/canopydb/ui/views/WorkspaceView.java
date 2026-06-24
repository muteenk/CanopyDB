package org.canopydb.ui.views;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.organisms.workspace.Sidebar;
import org.canopydb.ui.organisms.workspace.Workspace;

public class WorkspaceView implements View {
    private final BorderPane app = new BorderPane();

    public WorkspaceView() {
        Workspace workspace = new Workspace();
        Sidebar sidebar = new Sidebar(
                workspace::addNewSession,
                workspace::selectActiveSession
        );
        app.setLeft(sidebar.getSidebar());
        app.setCenter(workspace.getWorkspace());
    }

    public Parent getView() {return this.app;}
}
