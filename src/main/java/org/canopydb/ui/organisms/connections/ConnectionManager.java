package org.canopydb.ui.organisms.connections;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.SavedConnection;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.molecules.ConnectionCard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ConnectionManager {

    private final VBox connectionManagerArea = new VBox();
    private final VBox connectionList = new VBox(8);
    private final List<SavedConnection> connections = new ArrayList<>();
    private final Map<String, ConnectionCard> connectionCards = new LinkedHashMap<>();
    private final ConnectionFormArea formArea;

    private ConnectionCard selectedCard;
    private String searchQuery = "";

    public ConnectionManager(ConnectionFormArea formArea) {
        this.formArea = formArea;

        seedSampleConnections();
        wireFormCallbacks();

        TextField searchField = new TextInput("Search connections").getTextField();
        searchField.getStyleClass().add("connection-search");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchQuery = newVal == null ? "" : newVal.trim().toLowerCase(Locale.ROOT);
            refreshConnectionList();
        });

        Button newConnectionButton = new Button("+ New Connection");
        newConnectionButton.getStyleClass().add("connection-new-button");
        newConnectionButton.setMaxWidth(Double.MAX_VALUE);
        newConnectionButton.setOnAction(e -> {
            clearSelection();
            formArea.showConnectionForm();
        });

        connectionList.getStyleClass().add("connection-list");
        connectionList.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(connectionList);
        scrollPane.getStyleClass().add("connection-list-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        connectionManagerArea.getChildren().addAll(
                searchField,
                newConnectionButton,
                scrollPane
        );
        connectionManagerArea.getStyleClass().addAll("sidebar", "connection-sidebar");
        connectionManagerArea.setFillWidth(true);

        refreshConnectionList();
    }

    public VBox getConnectionManagerArea() {
        return connectionManagerArea;
    }

    private void seedSampleConnections() {
        connections.add(new SavedConnection(
                "Local MySQL",
                "localhost",
                3306,
                "root",
                ConnectionLabel.LOCAL
        ));
        connections.add(new SavedConnection(
                "Staging Postgres",
                "staging.db.internal",
                5432,
                "canopy",
                ConnectionLabel.DEV
        ));
        connections.add(new SavedConnection(
                "Analytics Warehouse",
                "warehouse.example.com",
                3306,
                "readonly",
                ConnectionLabel.PROD
        ));
    }

    private void wireFormCallbacks() {
        formArea.setOnSave(this::handleSave);
    }

    private void handleSave(SavedConnection connection) {
        SavedConnection existing = findConnectionById(connection.getId());
        if (existing != null) {
            existing.setName(connection.getName());
            existing.setHost(connection.getHost());
            existing.setPort(connection.getPort());
            existing.setUsername(connection.getUsername());
            existing.setPassword(connection.getPassword());
            existing.setLabel(connection.getLabel());
            connectionCards.get(existing.getId()).refresh();
        } else {
            connections.add(connection);
            refreshConnectionList();
            selectConnection(connection.getId());
        }
    }

    private void refreshConnectionList() {
        connectionList.getChildren().clear();
        connectionCards.clear();

        for (SavedConnection connection : connections) {
            if (!matchesSearch(connection)) {
                continue;
            }

            ConnectionCard card = new ConnectionCard(connection);
            card.getCard().setOnMouseClicked(e -> {
                selectConnection(connection.getId());
                formArea.showConnectionForm(connection);
            });
            connectionCards.put(connection.getId(), card);
            connectionList.getChildren().add(card.getCard());
        }

        if (selectedCard != null) {
            ConnectionCard refreshed = connectionCards.get(
                    selectedCard.getConnection().getId()
            );
            if (refreshed != null) {
                refreshed.setSelected(true);
                selectedCard = refreshed;
            } else {
                selectedCard = null;
            }
        }
    }

    private boolean matchesSearch(SavedConnection connection) {
        if (searchQuery.isEmpty()) {
            return true;
        }
        return connection.getName().toLowerCase(Locale.ROOT).contains(searchQuery)
                || connection.getHost().toLowerCase(Locale.ROOT).contains(searchQuery)
                || String.valueOf(connection.getPort()).contains(searchQuery)
                || connection.getLabel().getDisplayName()
                        .toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    private void selectConnection(String connectionId) {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }
        ConnectionCard card = connectionCards.get(connectionId);
        if (card != null) {
            card.setSelected(true);
            selectedCard = card;
        }
    }

    private void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
        }
    }

    private SavedConnection findConnectionById(String id) {
        for (SavedConnection connection : connections) {
            if (connection.getId().equals(id)) {
                return connection;
            }
        }
        return null;
    }
}
