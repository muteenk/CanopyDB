package org.canopydb.ui.scenes;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.interfaces.SceneInterface;
import org.canopydb.ui.molecules.Notification;
import org.canopydb.ui.organisms.Sidebar;
import org.canopydb.ui.organisms.Workspace;

public class WorkspaceScene implements SceneInterface {
    private final BorderPane app = new BorderPane();

    public WorkspaceScene(Notification notification) {
        Workspace workspace = new Workspace(
                notification::pushNotification
        );
        Sidebar sidebar = new Sidebar(
                workspace::addNewSession,
                workspace::isSessionActive,
                notification::pushNotification
        );
        app.setLeft(sidebar.getSidebar());
        app.setCenter(workspace.getWorkspace());
    }

    public Parent getScene() {return this.app;}
}
