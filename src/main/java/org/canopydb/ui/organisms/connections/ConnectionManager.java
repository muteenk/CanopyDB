package org.canopydb.ui.organisms.connections;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConnectionManager {
    private final VBox connectionManagerArea = new VBox();

    public ConnectionManager(ConnectionFormArea formArea) {
        Button addConnectionButton = new Button("+");
        addConnectionButton.setOnAction(e ->
                formArea.showConnectionForm());
        TextField searchConnections = new TextField("Search Connections");
        HBox connectionsTopbar = new HBox();
        connectionsTopbar.getChildren().addAll(addConnectionButton, searchConnections);
        VBox connectionsArea = new VBox();
        connectionsArea.getChildren().add(connectionsTopbar);

        connectionManagerArea.getChildren().add(connectionsArea);
    }

    public VBox getConnectionManagerArea() {return this.connectionManagerArea;}
}
