package org.canopydb.ui.scenes;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.canopydb.ui.interfaces.Scene;
import org.canopydb.ui.molecules.Notification;
import org.canopydb.ui.organisms.connections.ConnectionManager;

public class ConnectionScene implements Scene {
    private final BorderPane app = new BorderPane();

    public ConnectionScene() {
        ConnectionManager connectionManager = new ConnectionManager();
        app.setCenter(connectionManager.getConnectionManagerStack());
    }



    public Parent getScene() {return this.app;}
}
