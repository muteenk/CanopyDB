package org.canopydb.ui.views;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.organisms.connections.ConnectionFormArea;
import org.canopydb.ui.organisms.connections.ConnectionManager;

public class ConnectionView implements View {
    private final BorderPane app = new BorderPane();

    public ConnectionView() {
        ConnectionFormArea connectionFormArea = new ConnectionFormArea();
        ConnectionManager connectionManager = new ConnectionManager(connectionFormArea);
        app.setLeft(connectionManager.getConnectionManagerArea());
        app.setCenter(connectionFormArea.getConnectionFormArea());
    }

    public Parent getView() {return this.app;}
}
