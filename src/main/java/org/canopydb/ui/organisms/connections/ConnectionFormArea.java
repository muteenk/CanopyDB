package org.canopydb.ui.organisms.connections;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.SavedConnection;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.singletons.ViewManager;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.views.WorkspaceView;

import java.util.Objects;
import java.util.function.Consumer;

public class ConnectionFormArea {

    private final VBox connectionFormArea = new VBox();

    private Consumer<SavedConnection> onSave;

    public ConnectionFormArea() {
        connectionFormArea.getStyleClass().add("connection-content-area");
        connectionFormArea.setAlignment(Pos.CENTER);
        showWelcome();
    }

    public VBox getConnectionFormArea() {
        return connectionFormArea;
    }

    public void setOnSave(Consumer<SavedConnection> onSave) {
        this.onSave = onSave;
    }

    public void showWelcome() {
        connectionFormArea.getChildren().setAll(buildWelcome());
    }

    public void showConnectionForm() {
        connectionFormArea.getChildren().setAll(buildConnectionForm(null));
    }

    public void showConnectionForm(SavedConnection connection) {
        connectionFormArea.getChildren().setAll(buildConnectionForm(connection));
    }

    private VBox buildWelcome() {
        Image logoImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/assets/logo.png")
                )
        );

        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(320);
        logo.setPreserveRatio(true);
        logo.getStyleClass().add("connection-logo");

        Label subtitle = new Label(
                "Manage and connect to multiple databases through a "
                        + "lightweight modern desktop client."
        );
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(400);
        subtitle.setAlignment(Pos.CENTER);

        VBox welcome = new VBox(28, logo, subtitle);
        welcome.getStyleClass().add("connection-welcome");
        welcome.setAlignment(Pos.CENTER);

        return welcome;
    }

    private VBox buildConnectionForm(SavedConnection existing) {
        boolean isNew = existing == null;

        Label formTitle = new Label(isNew ? "New Connection" : "Edit Connection");
        formTitle.getStyleClass().add("connection-form-title");

        TextField nameField = new TextInput("My Database").getTextField();
        TextField hostField = new TextInput("localhost").getTextField();
        TextField portField = new TextInput("3306").getTextField();
        TextField usernameField = new TextInput("root").getTextField();
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<ConnectionLabel> labelField = new ComboBox<>();
        labelField.getItems().addAll(ConnectionLabel.values());
        labelField.setValue(ConnectionLabel.LOCAL);

        if (existing != null) {
            nameField.setText(existing.getName());
            hostField.setText(existing.getHost());
            portField.setText(String.valueOf(existing.getPort()));
            usernameField.setText(existing.getUsername());
            passwordField.setText(existing.getPassword());
            labelField.setValue(existing.getLabel());
        }

        VBox fields = new VBox(
                14,
                labeledField("Connection Name", nameField),
                labeledField("Environment", labelField),
                labeledField("Host", hostField),
                labeledField("Port", portField),
                labeledField("Username", usernameField),
                labeledField("Password", passwordField)
        );
        fields.getStyleClass().add("connection-form-fields");

        Button testButton = new Button("Test Connection");
        testButton.getStyleClass().addAll("connection-button", "connection-button-secondary");

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().addAll("connection-button", "connection-button-primary");

        Button connectButton = new Button("Connect");
        connectButton.getStyleClass().addAll("connection-button", "connection-button-primary");

        HBox actions = new HBox(10, testButton, saveButton, connectButton);
        actions.getStyleClass().add("connection-form-actions");
        actions.setAlignment(Pos.CENTER);

        testButton.setOnAction(e -> NotificationManager.pushNotification(
                "Connection Test",
                "Connection test is not wired yet.",
                NotificationManager.NotificationType.INFO
        ));

        saveButton.setOnAction(e -> {
            SavedConnection connection = buildConnectionFromForm(
                    existing,
                    nameField,
                    labelField,
                    hostField,
                    portField,
                    usernameField,
                    passwordField
            );
            if (onSave != null) {
                onSave.accept(connection);
            }
            NotificationManager.pushNotification(
                    "Connection Saved",
                    connection.getName() + " was saved successfully.",
                    NotificationManager.NotificationType.SUCCESS
            );
        });

        connectButton.setOnAction(e -> ViewManager.pushView(new WorkspaceView().getView()));

        VBox card = new VBox(20, formTitle, fields, actions);
        card.getStyleClass().add("connection-form-card");

        VBox form = new VBox(card);
        form.getStyleClass().add("connection-form");
        form.setAlignment(Pos.CENTER);

        return form;
    }

    private VBox labeledField(String labelText, Control input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("connection-form-label");

        if (input instanceof TextField textField) {
            textField.getStyleClass().add("connection-form-input");
        } else if (input instanceof PasswordField passwordField) {
            passwordField.getStyleClass().add("connection-form-input");
        } else if (input instanceof ComboBox<?> comboBox) {
            comboBox.getStyleClass().add("connection-form-combo");
        }

        VBox field = new VBox(6, label, input);
        field.getStyleClass().add("connection-form-field");
        return field;
    }

    private SavedConnection buildConnectionFromForm(
            SavedConnection existing,
            TextField nameField,
            ComboBox<ConnectionLabel> labelField,
            TextField hostField,
            TextField portField,
            TextField usernameField,
            PasswordField passwordField
    ) {
        String name = nameField.getText().trim();
        ConnectionLabel label = labelField.getValue() != null
                ? labelField.getValue()
                : ConnectionLabel.LOCAL;
        String host = hostField.getText().trim();
        int port = parsePort(portField.getText().trim());
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (existing != null) {
            return new SavedConnection(
                    existing.getId(),
                    name,
                    host,
                    port,
                    username,
                    password,
                    label
            );
        }

        return new SavedConnection(name, host, port, username, label);
    }

    private int parsePort(String portText) {
        try {
            return Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            return 3306;
        }
    }
}
