package org.canopydb.ui.organisms.connections;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.canopydb.ui.SceneManager;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.Scene;
import org.canopydb.ui.molecules.Notification;
import org.canopydb.ui.scenes.WorkspaceScene;

public class ConnectionManager {
    private final StackPane connectionManagerStack = new StackPane();

    public ConnectionManager() {
        Button addConnectionButton = new Button("+");
        addConnectionButton.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            connectionManagerStack.getChildren().add(newConnectionPopup());
        });
        TextField searchConnections = new TextField("Search Connections");
        HBox connectionsTopbar = new HBox();
        connectionsTopbar.getChildren().addAll(addConnectionButton, searchConnections);
        VBox connectionsArea = new VBox();
        connectionsArea.getChildren().add(connectionsTopbar);

        connectionManagerStack.getChildren().add(connectionsArea);
    }

    private VBox newConnectionPopup() {
        VBox newConnectionForm = new VBox();
        TextField host = new TextInput("Host/IP").getTextField();
        TextField port = new TextInput("Port (Default 3306)").getTextField();
        TextField username = new TextInput("Username").getTextField();
        TextField password = new PasswordField();
        password.setPromptText("Password");
        Button close = new Button("Close");
        Button testConnection = new Button("Test Connection");
        Button connect = new Button("Connect");
        close.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            connectionManagerStack.getChildren().removeLast();
        });
        connect.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            Scene workspaceScene = new WorkspaceScene();
            SceneManager.pushScene(workspaceScene.getScene());
        });

        newConnectionForm.getChildren().addAll(host, port, username, password, close, testConnection, connect);
        return newConnectionForm;
    }

    public StackPane getConnectionManagerStack() {return this.connectionManagerStack;}
}
