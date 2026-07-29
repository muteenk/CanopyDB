package org.canopydb.ui.views;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.organisms.connections.ConnectionFormArea;
import org.canopydb.ui.organisms.connections.ConnectionManager;

public class ConnectionView implements View {
    private final SplitPane app = new SplitPane();

    public ConnectionView() {
        ConnectionFormArea connectionFormArea = new ConnectionFormArea();
        ConnectionManager connectionManager = new ConnectionManager(connectionFormArea);

        VBox sidebar = connectionManager.getConnectionManagerArea();
        VBox formArea = connectionFormArea.getConnectionFormArea();

        VBox.setVgrow(sidebar, Priority.ALWAYS);
        VBox.setVgrow(formArea, Priority.ALWAYS);
        formArea.setMinWidth(360);

        app.getItems().addAll(sidebar, formArea);
        app.setOrientation(Orientation.HORIZONTAL);
        app.setDividerPositions(0.30);
        app.getStyleClass().add("connection-split");
    }

    public Parent getView() {
        return this.app;
    }
}
