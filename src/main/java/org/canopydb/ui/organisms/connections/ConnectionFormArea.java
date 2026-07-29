package org.canopydb.ui.organisms.connections;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.config.DatabasePool;
import org.canopydb.config.AppLogger;
import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.ConnectionMeta;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.singletons.ViewManager;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.views.WorkspaceView;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionFormArea {
    private static final Logger LOGGER = AppLogger.getLogger(ConnectionFormArea.class);
    private static final int CONNECTION_NAME_MAX_LENGTH = 48;

    private final VBox contentArea = new VBox();

    private Consumer<ConnectionMeta> onSave;

    public ConnectionFormArea() {
        contentArea.getStyleClass().add("connection-content-area");
        contentArea.setAlignment(Pos.CENTER);
        showWelcome();
    }

    public VBox getConnectionFormArea() {
        return contentArea;
    }

    public void setOnSave(Consumer<ConnectionMeta> onSave) {
        this.onSave = onSave;
    }

    public void showWelcome() {
        contentArea.getChildren().setAll(buildWelcome());
    }

    public void showConnectionForm() {
        contentArea.getChildren().setAll(buildConnectionForm(null));
    }

    public void showConnectionForm(ConnectionMeta connection) {
        contentArea.getChildren().setAll(buildConnectionForm(connection));
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

    private VBox buildConnectionForm(ConnectionMeta existing) {
        boolean isNew = existing == null;

        Label formTitle = new Label(isNew ? "New Connection" : "Edit Connection");
        formTitle.getStyleClass().add("connection-form-title");

        TextField nameField = new TextInput("My Database").getTextField();
        nameField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= CONNECTION_NAME_MAX_LENGTH
                        ? change
                        : null
        ));

        TextField hostField = new TextInput("localhost").getTextField();
        TextField portField = new TextInput("3306").getTextField();
        TextField usernameField = new TextInput("root").getTextField();
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        restrictPasswordFieldClipboard(passwordField);

        ComboBox<ConnectionLabel> labelField = new ComboBox<>();
        labelField.getItems().addAll(ConnectionLabel.values());
        labelField.setValue(ConnectionLabel.LOCAL);

        if (existing != null) {
            String existingName = existing.getName();
            if (existingName != null && existingName.length() > CONNECTION_NAME_MAX_LENGTH) {
                existingName = existingName.substring(0, CONNECTION_NAME_MAX_LENGTH);
            }
            nameField.setText(existingName);
            hostField.setText(existing.getHost());
            portField.setText(String.valueOf(existing.getPort()));
            usernameField.setText(existing.getUsername());
            passwordField.setText(existing.getPassword());
            labelField.setValue(existing.getLabel());
        }

        VBox fields = new VBox(
                14,
                labeledNameField(nameField),
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
        saveButton.setVisible(false);
        saveButton.setManaged(false);

        Button connectButton = new Button("Connect");
        connectButton.getStyleClass().addAll("connection-button", "connection-button-primary");

        HBox actions = new HBox(10, testButton, saveButton, connectButton);
        actions.getStyleClass().add("connection-form-actions");
        actions.setAlignment(Pos.CENTER);

        FormSnapshot baseline = snapshotForm(
                nameField,
                labelField,
                hostField,
                portField,
                usernameField,
                passwordField
        );

        Runnable updateSaveVisibility = () -> {
            if (isNew) {
                saveButton.setVisible(false);
                saveButton.setManaged(false);
                return;
            }
            boolean dirty = !baseline.equals(snapshotForm(
                    nameField,
                    labelField,
                    hostField,
                    portField,
                    usernameField,
                    passwordField
            ));
            saveButton.setVisible(dirty);
            saveButton.setManaged(dirty);
        };

        nameField.textProperty().addListener((o, a, b) -> updateSaveVisibility.run());
        hostField.textProperty().addListener((o, a, b) -> updateSaveVisibility.run());
        portField.textProperty().addListener((o, a, b) -> updateSaveVisibility.run());
        usernameField.textProperty().addListener((o, a, b) -> updateSaveVisibility.run());
        passwordField.textProperty().addListener((o, a, b) -> updateSaveVisibility.run());
        labelField.valueProperty().addListener((o, a, b) -> updateSaveVisibility.run());

        testButton.setOnAction(e -> NotificationManager.pushNotification(
                "Connection Test",
                "Connection test is not wired yet.",
                NotificationManager.NotificationType.INFO
        ));

        saveButton.setOnAction(e -> {
            ConnectionMeta connection = buildConnectionFromForm(
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

            baseline.copyFrom(snapshotForm(
                    nameField,
                    labelField,
                    hostField,
                    portField,
                    usernameField,
                    passwordField
            ));
            updateSaveVisibility.run();

            NotificationManager.pushNotification(
                    "Connection Saved",
                    connection.getName() + " was saved successfully.",
                    NotificationManager.NotificationType.SUCCESS
            );
        });

        connectButton.setOnAction(e -> {
            ConnectionMeta connection = buildConnectionFromForm(
                    existing,
                    nameField,
                    labelField,
                    hostField,
                    portField,
                    usernameField,
                    passwordField
            );

            try {
                DatabasePool.connect(connection);

                if (onSave != null) {
                    onSave.accept(connection);
                }

                NotificationManager.pushNotification(
                        "Connection Saved",
                        connection.getName() + " was saved successfully.",
                        NotificationManager.NotificationType.SUCCESS
                );

                ViewManager.pushView(new WorkspaceView().getView());

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Connection failed", ex);
                NotificationManager.pushNotification(
                        "Connection Failed",
                        ex.getMessage(),
                        NotificationManager.NotificationType.DANGER
                );
            }
        });

        VBox card = new VBox(20, formTitle, fields, actions);
        card.getStyleClass().add("connection-form-card");

        VBox form = new VBox(card);
        form.getStyleClass().add("connection-form");
        form.setAlignment(Pos.CENTER);

        return form;
    }

    private FormSnapshot snapshotForm(
            TextField nameField,
            ComboBox<ConnectionLabel> labelField,
            TextField hostField,
            TextField portField,
            TextField usernameField,
            PasswordField passwordField
    ) {
        return new FormSnapshot(
                nullToEmpty(nameField.getText()),
                labelField.getValue(),
                nullToEmpty(hostField.getText()),
                nullToEmpty(portField.getText()),
                nullToEmpty(usernameField.getText()),
                nullToEmpty(passwordField.getText())
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class FormSnapshot {
        private String name;
        private ConnectionLabel label;
        private String host;
        private String port;
        private String username;
        private String password;

        private FormSnapshot(
                String name,
                ConnectionLabel label,
                String host,
                String port,
                String username,
                String password
        ) {
            this.name = name;
            this.label = label;
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        private void copyFrom(FormSnapshot other) {
            this.name = other.name;
            this.label = other.label;
            this.host = other.host;
            this.port = other.port;
            this.username = other.username;
            this.password = other.password;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FormSnapshot other)) return false;
            return Objects.equals(name, other.name)
                    && label == other.label
                    && Objects.equals(host, other.host)
                    && Objects.equals(port, other.port)
                    && Objects.equals(username, other.username)
                    && Objects.equals(password, other.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, label, host, port, username, password);
        }
    }

    private void restrictPasswordFieldClipboard(PasswordField passwordField) {
        passwordField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.COPY || code == KeyCode.CUT) {
                event.consume();
                return;
            }

            // Ctrl/Cmd + C / X, and Ctrl + Insert (copy)
            if (event.isShortcutDown()
                    && (code == KeyCode.C || code == KeyCode.X || code == KeyCode.INSERT)) {
                event.consume();
                return;
            }

            // Shift + Delete (cut on some platforms)
            if (event.isShiftDown() && code == KeyCode.DELETE) {
                event.consume();
            }
        });

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> passwordField.paste());

        ContextMenu contextMenu = new ContextMenu(pasteItem);
        passwordField.setContextMenu(contextMenu);
    }

    private VBox labeledNameField(TextField nameField) {
        Label label = new Label("Connection Name");
        label.getStyleClass().add("connection-form-label");

        Label counter = new Label();
        counter.getStyleClass().add("connection-form-char-count");
        updateNameCharCount(counter, nameField.getText());

        nameField.textProperty().addListener((obs, oldVal, newVal) ->
                updateNameCharCount(counter, newVal)
        );

        HBox labelRow = new HBox(label, counter);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);

        nameField.getStyleClass().add("connection-form-input");

        VBox field = new VBox(6, labelRow, nameField);
        field.getStyleClass().add("connection-form-field");
        return field;
    }

    private void updateNameCharCount(Label counter, String text) {
        int length = text == null ? 0 : text.length();
        counter.setText(length + " / " + CONNECTION_NAME_MAX_LENGTH);
        if (length >= CONNECTION_NAME_MAX_LENGTH) {
            if (!counter.getStyleClass().contains("connection-form-char-count-limit")) {
                counter.getStyleClass().add("connection-form-char-count-limit");
            }
        } else {
            counter.getStyleClass().remove("connection-form-char-count-limit");
        }
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

    private ConnectionMeta buildConnectionFromForm(
            ConnectionMeta existing,
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
            return new ConnectionMeta(
                    existing.getId(),
                    name,
                    host,
                    port,
                    username,
                    password,
                    label
            );
        }

        return new ConnectionMeta(name, host, port, username, password, label);
    }

    private int parsePort(String portText) {
        try {
            return Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            return 3306;
        }
    }
}
