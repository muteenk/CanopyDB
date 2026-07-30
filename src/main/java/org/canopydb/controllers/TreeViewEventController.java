package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.utils.TreeViewComponent;
import org.canopydb.utils.Constants;
import org.canopydb.utils.ExceptionMessages;
import org.canopydb.utils.TableUtilities;


public class TreeViewEventController {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableOpenAction tableDataAppendAction;
    private final TableActiveCheck tableActiveCheck;

    private Runnable onTreeDataChanged;

    public TreeViewEventController(TableOpenAction tableDataAppendAction, TableActiveCheck tableActiveCheck){
        this.tableDataAppendAction = tableDataAppendAction;
        this.tableActiveCheck = tableActiveCheck;
    }

    public void setOnTreeDataChanged(Runnable onTreeDataChanged) {
        this.onTreeDataChanged = onTreeDataChanged;
    }

    private void notifyTreeDataChanged() {
        if (onTreeDataChanged != null) {
            onTreeDataChanged.run();
        }
    }

    public void dbExpandHandler(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals(Constants.LOADING) && !firstChildValue.equals(Constants.FAILED)){
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
                notifyTreeDataChanged();
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>(Constants.FAILED));
                NotificationManager.pushNotification(
                        "Failed to fetch tables !",
                        ExceptionMessages.userMessage(error),
                        NotificationManager.NotificationType.DANGER
                );
                notifyTreeDataChanged();
            });
            return null;
        });
    }

    public void dbRootExpandHandler(TreeItem<String> node) {
        if (node.getChildren().isEmpty()) return;
        String firstChildValue = node.getChildren().getFirst().getValue();
        if (!firstChildValue.equals(Constants.LOADING) &&
                !firstChildValue.equals(Constants.FAILED)){
            return;
        }

        connectionMetadataService
        .loadDatabaseAsync()
        .thenAccept(dbList -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                for (String dbName : dbList) {
                    TreeItem<String> dbItem = new TreeItem<>(dbName);
                    dbItem.getChildren().add(new TreeItem<>(Constants.LOADING));
                    dbItem.addEventHandler(TreeItem.<String>branchExpandedEvent(), dbEvent -> {
                        if (dbEvent.getSource() != dbItem) return;
                        dbExpandHandler(dbEvent.getSource());
                    });
                    node.getChildren().add(dbItem);
                }
                notifyTreeDataChanged();
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>(Constants.FAILED));
                NotificationManager.pushNotification(
                        "Failed to fetch databases !",
                        ExceptionMessages.userMessage(error),
                        NotificationManager.NotificationType.DANGER
                );
                notifyTreeDataChanged();
            });
            return null;
        });
    }

    public void nodeClickEventHandler(
            TreeItem<String> selectedItem
    ) {
        if (selectedItem != null && TreeViewComponent.isTableNode(selectedItem)) {
            if (tableActiveCheck.isActive(TableUtilities.tablePath(
                    selectedItem.getParent().getValue(),
                    selectedItem.getValue()
            ))) return;
            tableActionService.loadTableDataAsync(
                    selectedItem.getValue(),
                    selectedItem.getParent().getValue()
            ).thenAccept(session -> {
                Platform.runLater(() -> {
                    tableDataAppendAction.render(session);
                });
            }).exceptionally(error -> {
                Platform.runLater(() -> {
                    NotificationManager.pushNotification(
                            "Failed to fetch table !",
                            ExceptionMessages.userMessage(error),
                            NotificationManager.NotificationType.DANGER
                    );
                });
                return null;
            });
        }
    }
}
