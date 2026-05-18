package org.canopydb.ui;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.db.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sidebar {
    private Map<String, List<String>> loadDatabases() {
        Map<String, List<String>> databases = new HashMap<>();
        String sql = "SHOW DATABASES;";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    databases.put(rs.getString("Database"), new ArrayList<>(List.of("Fetching...")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return databases;
    }
    public VBox getSidebar() {
        Map<String, List<String>> databases = loadDatabases();
        TreeItem<String> rootDatabases = new TreeItem<>("Databases");

        databases.forEach((database, tables) -> {
            TreeItem<String> dbItem = new TreeItem<>(database);
            dbItem.getChildren().addAll(
                    tables.stream()
                            .map(TreeItem::new)
                            .toList()
            );
            rootDatabases.getChildren().add(dbItem);
        });

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search");
        searchInput.setStyle("-fx-background-color: #363840;");

        TreeView<String> databaseTreeView = new TreeView<>(rootDatabases);
        databaseTreeView.setStyle("-fx-background-color: #363840;");
        VBox sidebar = new VBox(searchInput, databaseTreeView);
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        sidebar.setStyle("-fx-background-color: #363840;");
        return sidebar;
    }
}
