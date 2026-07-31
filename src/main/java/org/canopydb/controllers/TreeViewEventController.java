package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.utils.LazyTreeItem;
import org.canopydb.ui.utils.TreeViewComponent;
import org.canopydb.utils.ExceptionMessages;
import org.canopydb.utils.TableUtilities;

public class TreeViewEventController {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableOpenAction tableDataAppendAction;
    private final TableActiveCheck tableActiveCheck;

    private Runnable onTreeDataChanged;

    public TreeViewEventController(TableOpenAction tableDataAppendAction, TableActiveCheck tableActiveCheck) {
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

    /**
     * Loads tables into {@code dataNode}. Spinner shows on {@code uiNode}
     * (same node in the normal tree; may differ while search projection is visible).
     */
    public void dbExpandHandler(LazyTreeItem dataNode, LazyTreeItem uiNode) {
        if (dataNode == null || dataNode.isLoading() || !dataNode.needsLoad()) {
            return;
        }

        dataNode.beginLoading();
        if (uiNode != null && uiNode != dataNode) {
            uiNode.beginLoading();
        }

        connectionMetadataService
                .loadDBTablesAsync(dataNode.getValue())
                .thenAccept(tables -> Platform.runLater(() -> {
                    dataNode.getChildren().clear();
                    for (String table : tables) {
                        dataNode.getChildren().add(new TreeItem<>(table));
                    }
                    dataNode.markLoaded();
                    if (uiNode != null && uiNode != dataNode) {
                        uiNode.markLoaded();
                    }
                    notifyTreeDataChanged();
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        dataNode.markUnloaded();
                        dataNode.setExpanded(false);
                        if (uiNode != null && uiNode != dataNode) {
                            uiNode.markUnloaded();
                            uiNode.setExpanded(false);
                        }
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

    public void dbExpandHandler(LazyTreeItem node) {
        dbExpandHandler(node, node);
    }

    public void dbRootExpandHandler(LazyTreeItem node) {
        if (node == null || node.isLoading() || !node.needsLoad()) {
            return;
        }

        node.beginLoading();

        connectionMetadataService
                .loadDatabaseAsync()
                .thenAccept(dbList -> Platform.runLater(() -> {
                    node.getChildren().clear();
                    for (String dbName : dbList) {
                        LazyTreeItem dbItem = new LazyTreeItem(dbName);
                        dbItem.addEventHandler(TreeItem.<String>branchExpandedEvent(), dbEvent -> {
                            if (dbEvent.getSource() != dbItem) {
                                return;
                            }
                            dbExpandHandler(dbItem);
                        });
                        node.getChildren().add(dbItem);
                    }
                    node.markLoaded();
                    notifyTreeDataChanged();
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        node.markUnloaded();
                        node.setExpanded(false);
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

    public void nodeClickEventHandler(TreeItem<String> selectedItem) {
        if (selectedItem != null && TreeViewComponent.isTableNode(selectedItem)) {
            if (tableActiveCheck.isActive(TableUtilities.tablePath(
                    selectedItem.getParent().getValue(),
                    selectedItem.getValue()
            ))) {
                return;
            }
            tableActionService.loadTableDataAsync(
                    selectedItem.getValue(),
                    selectedItem.getParent().getValue()
            ).thenAccept(session -> Platform.runLater(() ->
                    tableDataAppendAction.render(session)
            )).exceptionally(error -> {
                Platform.runLater(() -> NotificationManager.pushNotification(
                        "Failed to fetch table !",
                        ExceptionMessages.userMessage(error),
                        NotificationManager.NotificationType.DANGER
                ));
                return null;
            });
        }
    }
}
