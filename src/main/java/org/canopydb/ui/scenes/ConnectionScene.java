package org.canopydb.ui.scenes;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.interfaces.Scene;
import org.canopydb.ui.organisms.connections.ConnectionFormArea;
import org.canopydb.ui.organisms.connections.ConnectionManager;

public class ConnectionScene implements Scene {
    private final BorderPane app = new BorderPane();

    public ConnectionScene() {
        ConnectionManager connectionManager = new ConnectionManager();
        ConnectionFormArea connectionFormArea = new ConnectionFormArea();
        app.setLeft(connectionManager.getConnectionManagerArea());
        app.setCenter(connectionFormArea.getConnectionFormArea());
    }

    public Parent getScene() {return this.app;}
}
