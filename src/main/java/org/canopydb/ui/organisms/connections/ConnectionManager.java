package org.canopydb.ui.organisms.connections;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.models.ConnectionLabel;
import org.canopydb.models.ConnectionMeta;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.molecules.ConnectionCard;
import org.canopydb.config.AppLogger;
import org.canopydb.ui.singletons.NotificationManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionManager {
    private static final Logger LOGGER = AppLogger.getLogger(ConnectionManager.class);

    private final VBox connectionManagerArea = new VBox();
    private final VBox connectionList = new VBox(8);
    private List<ConnectionMeta> connections = new ArrayList<>();
    private final Map<String, ConnectionCard> connectionCards = new LinkedHashMap<>();
    private final ConnectionFormArea formArea;

    private ConnectionCard selectedCard;
    private String searchQuery = "";

    public ConnectionManager(ConnectionFormArea formArea) {
        this.formArea = formArea;

        seedSavedConnections();
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
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinWidth(0);
        scrollPane.setMinHeight(0);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        connectionManagerArea.getChildren().addAll(
                searchField,
                newConnectionButton,
                scrollPane
        );
        connectionManagerArea.getStyleClass().addAll("sidebar", "connection-sidebar");
        connectionManagerArea.setFillWidth(true);
        connectionManagerArea.setMinWidth(0);
        connectionManagerArea.setMinHeight(0);

        refreshConnectionList();
    }

    public VBox getConnectionManagerArea() {
        return connectionManagerArea;
    }

    private void seedSavedConnections() {
        ObjectMapper mapper = new ObjectMapper();

        // Resolves the '~' home directory properly across Windows, Mac, and Linux
        String userHome = System.getProperty("user.home");
        Path filePath = Paths.get(userHome, ".canopydb", "connections.json");
        File jsonFile = filePath.toFile();

        try {
            if (!jsonFile.exists()) {
                LOGGER.info("Connections file not found. Creating default configuration...");

                // Create parent directories (~/.canopydb) if they don't exist
                Files.createDirectories(filePath.getParent());

                List<ConnectionMeta> defaultConnections = new ArrayList<>();
                defaultConnections.add(new ConnectionMeta(
                        "Local Instance",
                        "localhost",
                        3306,
                        "root",
                        "",
                        ConnectionLabel.LOCAL
                ));

                // Save to file
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, defaultConnections);
                LOGGER.info("Created connections file at: " + filePath.toAbsolutePath());
            }

            // Read and parse the file
            // Using TypeFactory to handle a List of ConnectionConfig objects safely
            this.connections = mapper.readValue(
                    jsonFile,
                    mapper.getTypeFactory().constructCollectionType(List.class, ConnectionMeta.class)
            );

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error processing configuration file", e);
            NotificationManager.pushNotification(
                    "Failed to load existing connections",
                    "Could not load connections, unable to parse connections file",
                    NotificationManager.NotificationType.DANGER
            );
        }
    }

    private void wireFormCallbacks() {
        formArea.setOnSave(this::handleSave);
    }

    private void handleSave(ConnectionMeta connection) {
        ConnectionMeta existing = findConnectionById(connection.getId());
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

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Resolves the '~' home directory properly across Windows, Mac, and Linux
            String userHome = System.getProperty("user.home");
            Path filePath = Paths.get(userHome, ".canopydb", "connections.json");
            File jsonFile = filePath.toFile();
            Files.createDirectories(filePath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, connections);
        } catch (IOException ignored) {
            NotificationManager.pushNotification(
                    "Could not save connections",
                    "Unable to save connection details to CanopyDB",
                    NotificationManager.NotificationType.DANGER
            );
        }
    }

    private void refreshConnectionList() {
        connectionList.getChildren().clear();
        connectionCards.clear();

        for (ConnectionMeta connection : connections) {
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

    private boolean matchesSearch(ConnectionMeta connection) {
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

    private ConnectionMeta findConnectionById(String id) {
        for (ConnectionMeta connection : connections) {
            if (connection.getId().equals(id)) {
                return connection;
            }
        }
        return null;
    }
}
