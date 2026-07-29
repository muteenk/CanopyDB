package org.canopydb.ui.organisms.connections;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.ConnectionMeta;
import org.canopydb.ui.atoms.TextInput;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Connection form UI: fields, basic input constraints, dirty tracking, and action buttons.
 * Callers provide business logic for Test / Save / Connect via callbacks.
 */
public class ConnectionForm {

    private static final int CONNECTION_NAME_MAX_LENGTH = 48;

    private final ConnectionMeta existing;
    private final boolean isNew;

    private final TextField nameField;
    private final TextField hostField;
    private final TextField portField;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final ComboBox<ConnectionLabel> labelField;

    private final Button testButton;
    private final Button saveButton;
    private final Button connectButton;

    private final VBox root;
    private final FormSnapshot baseline;

    public ConnectionForm(ConnectionMeta existing) {
        this.existing = existing;
        this.isNew = existing == null;

        Label formTitle = new Label(isNew ? "New Connection" : "Edit Connection");
        formTitle.getStyleClass().add("connection-form-title");

        nameField = new TextInput("My Database").getTextField();
        nameField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= CONNECTION_NAME_MAX_LENGTH
                        ? change
                        : null
        ));

        hostField = new TextInput("localhost").getTextField();
        portField = new TextInput("3306").getTextField();
        usernameField = new TextInput("root").getTextField();
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        restrictPasswordFieldClipboard(passwordField);

        labelField = new ComboBox<>();
        labelField.getItems().addAll(ConnectionLabel.values());
        labelField.setValue(ConnectionLabel.LOCAL);

        if (existing != null) {
            populateFrom(existing);
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

        testButton = new Button("Test Connection");
        testButton.getStyleClass().addAll("connection-button", "connection-button-secondary");

        saveButton = new Button("Save");
        saveButton.getStyleClass().addAll("connection-button", "connection-button-primary");
        saveButton.setVisible(false);
        saveButton.setManaged(false);

        connectButton = new Button("Connect");
        connectButton.getStyleClass().addAll("connection-button", "connection-button-primary");

        HBox actions = new HBox(10, testButton, saveButton, connectButton);
        actions.getStyleClass().add("connection-form-actions");
        actions.setAlignment(Pos.CENTER);

        baseline = snapshotForm();
        wireDirtyTracking();

        VBox card = new VBox(20, formTitle, fields, actions);
        card.getStyleClass().add("connection-form-card");

        root = new VBox(card);
        root.getStyleClass().add("connection-form");
        root.setAlignment(Pos.CENTER);
    }

    public VBox getRoot() {
        return root;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setOnTest(Runnable action) {
        testButton.setOnAction(e -> action.run());
    }

    public void setOnSave(Consumer<ConnectionMeta> action) {
        saveButton.setOnAction(e -> action.accept(buildConnection()));
    }

    public void setOnConnect(Consumer<ConnectionMeta> action) {
        connectButton.setOnAction(e -> action.accept(buildConnection()));
    }

    /** Call after a successful save so the Save button hides until the next edit. */
    public void markClean() {
        baseline.copyFrom(snapshotForm());
        updateSaveVisibility();
    }

    public ConnectionMeta buildConnection() {
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

    private void populateFrom(ConnectionMeta connection) {
        String name = connection.getName();
        if (name != null && name.length() > CONNECTION_NAME_MAX_LENGTH) {
            name = name.substring(0, CONNECTION_NAME_MAX_LENGTH);
        }
        nameField.setText(name);
        hostField.setText(connection.getHost());
        portField.setText(String.valueOf(connection.getPort()));
        usernameField.setText(connection.getUsername());
        passwordField.setText(connection.getPassword());
        labelField.setValue(connection.getLabel());
    }

    private void wireDirtyTracking() {
        Runnable onChange = this::updateSaveVisibility;
        nameField.textProperty().addListener((o, a, b) -> onChange.run());
        hostField.textProperty().addListener((o, a, b) -> onChange.run());
        portField.textProperty().addListener((o, a, b) -> onChange.run());
        usernameField.textProperty().addListener((o, a, b) -> onChange.run());
        passwordField.textProperty().addListener((o, a, b) -> onChange.run());
        labelField.valueProperty().addListener((o, a, b) -> onChange.run());
    }

    private void updateSaveVisibility() {
        if (isNew) {
            saveButton.setVisible(false);
            saveButton.setManaged(false);
            return;
        }
        boolean dirty = !baseline.equals(snapshotForm());
        saveButton.setVisible(dirty);
        saveButton.setManaged(dirty);
    }

    private FormSnapshot snapshotForm() {
        return new FormSnapshot(
                nullToEmpty(nameField.getText()),
                labelField.getValue(),
                nullToEmpty(hostField.getText()),
                nullToEmpty(portField.getText()),
                nullToEmpty(usernameField.getText()),
                nullToEmpty(passwordField.getText())
        );
    }

    private void restrictPasswordFieldClipboard(PasswordField field) {
        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.COPY || code == KeyCode.CUT) {
                event.consume();
                return;
            }

            if (event.isShortcutDown()
                    && (code == KeyCode.C || code == KeyCode.X || code == KeyCode.INSERT)) {
                event.consume();
                return;
            }

            if (event.isShiftDown() && code == KeyCode.DELETE) {
                event.consume();
            }
        });

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> field.paste());
        field.setContextMenu(new ContextMenu(pasteItem));
    }

    private VBox labeledNameField(TextField field) {
        Label label = new Label("Connection Name");
        label.getStyleClass().add("connection-form-label");

        Label counter = new Label();
        counter.getStyleClass().add("connection-form-char-count");
        updateNameCharCount(counter, field.getText());

        field.textProperty().addListener((obs, oldVal, newVal) ->
                updateNameCharCount(counter, newVal)
        );

        HBox labelRow = new HBox(label, counter);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);

        field.getStyleClass().add("connection-form-input");

        VBox fieldBox = new VBox(6, labelRow, field);
        fieldBox.getStyleClass().add("connection-form-field");
        return fieldBox;
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
        } else if (input instanceof PasswordField pwField) {
            pwField.getStyleClass().add("connection-form-input");
        } else if (input instanceof ComboBox<?> comboBox) {
            comboBox.getStyleClass().add("connection-form-combo");
        }

        VBox field = new VBox(6, label, input);
        field.getStyleClass().add("connection-form-field");
        return field;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static int parsePort(String portText) {
        try {
            return Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            return 3306;
        }
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
}
