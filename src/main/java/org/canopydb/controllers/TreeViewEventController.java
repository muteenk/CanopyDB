package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.singletons.LoadingManager;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.utils.LazyTreeItem;
import org.canopydb.ui.utils.TreeViewComponent;
import org.canopydb.utils.ExceptionMessages;
import org.canopydb.utils.TableUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<List<String>> fetchAllDatabasesAsync() {
        return connectionMetadataService.loadDatabaseAsync();
    }

    /**
     * Adds a database node under the hidden tree root if it is not already pinned.
     *
     * @return the new node, or {@code null} if the database was already in the tree
     */
    public LazyTreeItem addPinnedDatabase(TreeItem<String> databasesRoot, String databaseName) {
        if (databaseName == null || databaseName.isBlank()) {
            return null;
        }
        for (TreeItem<String> child : databasesRoot.getChildren()) {
            if (databaseName.equals(child.getValue())) {
                return null;
            }
        }
        LazyTreeItem dbItem = createDatabaseNode(databaseName);
        databasesRoot.getChildren().add(dbItem);
        notifyTreeDataChanged();
        return dbItem;
    }

    /**
     * Reloads table lists for every pinned database that has been expanded before.
     */
    public void refreshPinnedDatabases(TreeItem<String> databasesRoot) {
        List<LazyTreeItem> pinned = new ArrayList<>();
        for (TreeItem<String> child : databasesRoot.getChildren()) {
            if (child instanceof LazyTreeItem lazy && lazy.isLoaded()) {
                pinned.add(lazy);
            }
        }
        if (pinned.isEmpty()) {
            NotificationManager.pushNotification(
                    "Nothing to refresh",
                    "Add a database and expand it to load tables first.",
                    NotificationManager.NotificationType.INFO
            );
            return;
        }
        for (LazyTreeItem dbItem : pinned) {
            reloadTables(dbItem);
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

    public void reloadTables(LazyTreeItem node) {
        if (node == null || node.isLoading()) {
            return;
        }
        node.markUnloaded();
        dbExpandHandler(node);
    }

    private LazyTreeItem createDatabaseNode(String dbName) {
        LazyTreeItem dbItem = new LazyTreeItem(dbName);
        dbItem.addEventHandler(TreeItem.<String>branchExpandedEvent(), dbEvent -> {
            if (dbEvent.getSource() != dbItem) {
                return;
            }
            dbExpandHandler(dbItem);
        });
        return dbItem;
    }

    public void nodeClickEventHandler(TreeItem<String> selectedItem) {
        if (selectedItem != null && TreeViewComponent.isTableNode(selectedItem)) {
            if (tableActiveCheck.isActive(TableUtilities.tablePath(
                    selectedItem.getParent().getValue(),
                    selectedItem.getValue()
            ))) {
                return;
            }
            LoadingManager.start();
            tableActionService.loadTableDataAsync(
                    selectedItem.getValue(),
                    selectedItem.getParent().getValue()
            ).whenComplete((session, error) -> LoadingManager.stop())
                    .thenAccept(session -> Platform.runLater(() ->
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
