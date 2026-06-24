package org.canopydb.ui.organisms.connections;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.canopydb.ui.singletons.SceneManager;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.Scene;
import org.canopydb.ui.scenes.WorkspaceScene;

public class ConnectionFormArea {
    private final VBox connectionFormArea = new VBox();

    public ConnectionFormArea() {
        TextField host = new TextInput("Host/IP").getTextField();
        TextField port = new TextInput("Port (Default 3306)").getTextField();
        TextField username = new TextInput("Username").getTextField();
        TextField password = new PasswordField();
        password.setPromptText("Password");
        Button close = new Button("Close");
        Button testConnection = new Button("Test Connection");
        Button connect = new Button("Connect");
        connect.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            Scene workspaceScene = new WorkspaceScene();
            SceneManager.pushScene(workspaceScene.getScene());
        });

        connectionFormArea.getChildren().addAll(host, port, username, password, close, testConnection, connect);
    }

    public VBox getConnectionFormArea() {return this.connectionFormArea;}
}
