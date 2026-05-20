package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableDataAppendAction;

public class TreeViewEventController {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableDataAppendAction tableDataAppendAction;
    private final TableActiveCheck tableActiveCheck;

    public TreeViewEventController(TableDataAppendAction tableDataAppendAction, TableActiveCheck tableActiveCheck){
        this.tableDataAppendAction = tableDataAppendAction;
        this.tableActiveCheck = tableActiveCheck;
    }

    private int getNodeDepth(TreeItem<String> item){
        // Level 0 = Root Server ("Databases" Root)
        // Level 1 = Database Node ("Database")
        // Level 2 = Table Node

        int depth = 0;
        TreeItem<String> current = item;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }

        return depth;
    }

    private boolean isTableNode(TreeItem<String> item) {
        return getNodeDepth(item) == 2;
    }

    public void dbExpandHandler(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") && !firstChildValue.equals("Error")){
            return;
        }

        connectionMetadataService
        .loadDBTablesAsync(node.getValue())
        .thenAccept(tables -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                for (String table: tables){
                    TreeItem<String> tableItem = new TreeItem<>(table);
                    node.getChildren().add(tableItem);
                }
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                System.err.println("Failed to fetch tables: " + error.getMessage());
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>("Error"));
            });
            return null;
        });
    }

    public void dbRootExpandHandler(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals("Loading") &&
                !firstChildValue.equals("Error")){
            return;
        }

        connectionMetadataService
        .loadDatabaseAsync()
        .thenAccept(dbList -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                for (String dbName : dbList) {
                    TreeItem<String> dbItem = new TreeItem<>(dbName);
                    dbItem.getChildren().add(new TreeItem<>("Loading"));
                    dbItem.addEventHandler(TreeItem.<String>branchExpandedEvent(), dbEvent -> {
                        dbExpandHandler(dbEvent.getSource());
                    });
                    node.getChildren().add(dbItem);
                }
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                System.err.println("Failed to fetch databases: " + error.getMessage());
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>("Error"));
            });
            return null;
        });
    }

    public void nodeClickEventHandler(
            TreeItem<String> selectedItem
    ) {
        if (selectedItem != null && isTableNode(selectedItem)) {
            if (tableActiveCheck.isActive(selectedItem.getValue())) return;
            tableActionService.loadTableDataAsync(
                    selectedItem.getValue(),
                    selectedItem.getParent().getValue()
            ).thenAccept(data -> {
                Platform.runLater(() -> {
                    tableDataAppendAction.render(data);
                });
            });
        }
    }
}
