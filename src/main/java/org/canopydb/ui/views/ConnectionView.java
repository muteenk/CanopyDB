package org.canopydb.ui.views;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.organisms.connections.ConnectionFormArea;
import org.canopydb.ui.organisms.connections.ConnectionManager;

public class ConnectionView implements View {
    private final SplitPane app = new SplitPane();

    public ConnectionView() {
        ConnectionFormArea connectionFormArea = new ConnectionFormArea();
        ConnectionManager connectionManager = new ConnectionManager(connectionFormArea);

        Region sidebar = connectionManager.getConnectionManagerArea();
        Region formArea = connectionFormArea.getConnectionFormArea();

        // Allow SplitPane panes to shrink below their preferred size so scrollbars can take over.
        sidebar.setMinWidth(0);
        sidebar.setMinHeight(0);
        formArea.setMinWidth(0);
        formArea.setMinHeight(0);

        app.getItems().addAll(sidebar, formArea);
        app.setOrientation(Orientation.HORIZONTAL);
        app.setDividerPositions(0.30);
        app.getStyleClass().add("connection-split");
    }

    public Parent getView() {
        return this.app;
    }
}
