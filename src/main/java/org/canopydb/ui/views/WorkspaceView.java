package org.canopydb.ui.views;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.canopydb.models.ConnectionMeta;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.organisms.workspace.Sidebar;
import org.canopydb.ui.organisms.workspace.Topbar;
import org.canopydb.ui.organisms.workspace.Workspace;

public class WorkspaceView implements View {
    private final BorderPane app = new BorderPane();

    public WorkspaceView() {
        this(null);
    }

    public WorkspaceView(ConnectionMeta connection) {
        Workspace workspace = new Workspace();
        Topbar topbar = new Topbar();
        Sidebar sidebar = new Sidebar(
                workspace::addNewSession,
                workspace::selectActiveSession,
                connection != null ? connection.getDatabase() : null
        );

        Region sidebarPane = sidebar.getSidebar();
        Region workspacePane = workspace.getWorkspace();

        // Let SplitPane honor Sidebar min/max instead of blocking on preferred sizes.
        sidebarPane.setMinHeight(0);
        workspacePane.setMinWidth(0);
        workspacePane.setMinHeight(0);

        SplitPane body = new SplitPane(sidebarPane, workspacePane);
        body.setOrientation(Orientation.HORIZONTAL);
        body.setDividerPositions(
                Sidebar.PREF_WIDTH / (Sidebar.PREF_WIDTH + 800)
        );
        body.getStyleClass().add("workspace-split");

        app.setTop(topbar.getTopbar());
        app.setCenter(body);
    }

    public Parent getView() {
        return this.app;
    }
}
