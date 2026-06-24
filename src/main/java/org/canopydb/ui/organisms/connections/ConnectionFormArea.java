package org.canopydb.ui.organisms.connections;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.canopydb.ui.singletons.ViewManager;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.interfaces.View;
import org.canopydb.ui.views.WorkspaceView;

public class ConnectionFormArea {

    private final VBox connectionFormArea = new VBox();

    public ConnectionFormArea() {
        showWelcome();
    }

    public VBox getConnectionFormArea() {
        return connectionFormArea;
    }

    public void showWelcome() {
        connectionFormArea.getChildren().setAll(buildWelcome());
    }

    public void showConnectionForm() {
        connectionFormArea.getChildren().setAll(buildConnectionForm());
    }

    private VBox buildWelcome() {
        VBox welcome = new VBox(15);

        Label logo = new Label("🌲");
        logo.getStyleClass().add("welcome-logo");

        Label title = new Label("CanopyDB");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label(
                "A lightweight SQL client.\nSelect a connection or create a new one."
        );
        subtitle.getStyleClass().add("welcome-subtitle");

        welcome.getChildren().addAll(
                logo,
                title,
                subtitle
        );

        welcome.setAlignment(Pos.CENTER);

        return welcome;
    }

    private VBox buildConnectionForm() {

        TextField host = new TextInput("Host / IP").getTextField();

        TextField port =
                new TextInput("Port (3306)").getTextField();

        TextField username =
                new TextInput("Username").getTextField();

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button close = new Button("Close");
        Button test = new Button("Test Connection");
        Button connect = new Button("Connect");

        connect.setOnAction(e -> {
            ViewManager.pushView(
                    new WorkspaceView().getView()
            );
        });

        close.setOnAction(e -> showWelcome());

        VBox form = new VBox(
                12,
                host,
                port,
                username,
                password,
                test,
                connect,
                close
        );

        form.getStyleClass().add("connection-form");
        form.setAlignment(Pos.CENTER);

        return form;
    }
}