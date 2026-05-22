package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.PushNotification;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.molecules.Notification;
import org.canopydb.ui.utils.TreeViewComponent;
import org.canopydb.utils.Constants;
import org.canopydb.utils.TableUtilities;


public class TreeViewEventController {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableOpenAction tableDataAppendAction;
    private final TableActiveCheck tableActiveCheck;
    private final PushNotification pushNotification;

    public TreeViewEventController(TableOpenAction tableDataAppendAction, TableActiveCheck tableActiveCheck, PushNotification pushNotification){
        this.tableDataAppendAction = tableDataAppendAction;
        this.tableActiveCheck = tableActiveCheck;
        this.pushNotification = pushNotification;
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
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>(Constants.FAILED));
                pushNotification.push(
                        "Failed to fetch tables !",
                        error.getMessage(),
                        Notification.NotificationType.DANGER
                );
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
            });
        })
        .exceptionally(error -> {
            Platform.runLater(() -> {
                if (!node.getChildren().isEmpty()) node.getChildren().clear();
                node.getChildren().add(new TreeItem<>(Constants.FAILED));
                pushNotification.push(
                        "Failed to fetch databases !",
                        error.getMessage(),
                        Notification.NotificationType.DANGER
                );
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
                    pushNotification.push(
                            "Failed to fetch table !",
                            error.getMessage(),
                            Notification.NotificationType.DANGER
                    );
                });
                return null;
            });
        }
    }
}
