package org.canopydb;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Map<String, List<String>> databases = new HashMap<>();
        databases.put("Database1", new ArrayList<>(List.of("D1T1", "D1T2", "D1T3")));
        databases.put("Database2", new ArrayList<>(List.of("D2T1", "D2T2")));
        databases.put("Database3", new ArrayList<>(List.of("D3T0", "D3T1", "D3T2")));

        TreeItem<String> rootDatabases = new TreeItem<>("Databases");
        databases.forEach((database, tables) -> {
            TreeItem<String> dbItem = new TreeItem<>(database);
            dbItem.getChildren().addAll(
                    tables.stream()
                            .map(TreeItem::new)
                            .toList()
            );

            // Attach this database node to the main root
            rootDatabases.getChildren().add(dbItem);
        });

        BorderPane borderPane = new BorderPane();
        TreeView<String> sidebar = new TreeView<>(rootDatabases);
        borderPane.setLeft(sidebar);
        Scene scene = new Scene(borderPane, 1280, 720,
                Color.DARKGRAY);

        stage.setTitle("CanopyDB - Lite SQL Client");
        stage.setScene(scene);
        stage.show();
    }

    static void main() {
        launch();
    }
}
