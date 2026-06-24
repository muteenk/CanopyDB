package org.canopydb.ui.molecules;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.SavedConnection;

public class ConnectionCard {

    private static final String CONNECTION_LABEL_SELECTED = "connection-card-selected";

    private final VBox card;
    private final SavedConnection connection;
    private final Label envLabel;
    private final Label nameLabel;
    private final Label hostLabel;

    public ConnectionCard(SavedConnection connection) {
        this.connection = connection;

        envLabel = new Label(connection.getLabel().getDisplayName());
        envLabel.getStyleClass().add("connection-label");
        applyLabelStyle();

        nameLabel = new Label(connection.getName());
        nameLabel.getStyleClass().add("connection-card-name");

        HBox nameRow = new HBox(10, envLabel, nameLabel);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        hostLabel = new Label(connection.getHostPort());
        hostLabel.getStyleClass().add("connection-card-host");

        card = new VBox(6, nameRow, hostLabel);
        card.getStyleClass().add("connection-card");
    }

    public VBox getCard() {
        return card;
    }

    public SavedConnection getConnection() {
        return connection;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            if (!card.getStyleClass().contains(CONNECTION_LABEL_SELECTED)) {
                card.getStyleClass().add(CONNECTION_LABEL_SELECTED);
            }
        } else {
            card.getStyleClass().remove(CONNECTION_LABEL_SELECTED);
        }
    }

    public void refresh() {
        nameLabel.setText(connection.getName());
        hostLabel.setText(connection.getHostPort());
        envLabel.setText(connection.getLabel().getDisplayName());
        applyLabelStyle();
    }

    private void applyLabelStyle() {
        for (ConnectionLabel label : ConnectionLabel.values()) {
            envLabel.getStyleClass().remove(label.getStyleClass());
        }
        envLabel.getStyleClass().add(connection.getLabel().getStyleClass());
    }
}
