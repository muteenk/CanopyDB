package org.canopydb.controllers;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.canopydb.services.AsyncQuery;
import org.canopydb.services.ConnectionMetadataService;
import org.canopydb.services.TableActionService;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.singletons.LoadingManager;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.utils.LazyTreeItem;
import org.canopydb.ui.utils.TreeViewComponent;
import org.canopydb.utils.ExceptionMessages;
import org.canopydb.utils.QueryExceptions;
import org.canopydb.utils.TableUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeViewEventController {
    private final ConnectionMetadataService connectionMetadataService = new ConnectionMetadataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableOpenAction tableDataAppendAction;
    private final TableActiveCheck tableActiveCheck;

    private final Map<String, AsyncQuery<List<String>>> inFlightTableLoads = new ConcurrentHashMap<>();
    private AsyncQuery<org.canopydb.models.TableSession> inFlightTableOpen;

    private Runnable onTreeDataChanged;

    public TreeViewEventController(TableOpenAction tableOpenAction, TableActiveCheck tableActiveCheck) {
        this.tableDataAppendAction = tableOpenAction;
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

    public AsyncQuery<List<String>> fetchAllDatabasesAsync() {
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

        String database = dataNode.getValue();
        cancelTableLoad(database);

        AsyncQuery<List<String>> query = connectionMetadataService.loadDBTablesAsync(database);
        inFlightTableLoads.put(database, query);

        query.future()
                .whenComplete((tables, error) -> inFlightTableLoads.remove(database, query))
                .thenAccept(tables -> Platform.runLater(() -> {
                    if (dataNode.isLoading()) {
                        dataNode.getChildren().clear();
                        for (String table : tables) {
                            dataNode.getChildren().add(new TreeItem<>(table));
                        }
                        dataNode.markLoaded();
                        if (uiNode != null && uiNode != dataNode) {
                            uiNode.markLoaded();
                        }
                        notifyTreeDataChanged();
                    }
                }))
                .exceptionally(error -> {
                    if (QueryExceptions.isCancellation(error)) {
                        return null;
                    }
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
        dbItem.addEventHandler(TreeItem.<String>branchCollapsedEvent(), dbEvent -> {
            if (dbEvent.getSource() != dbItem) {
                return;
            }
            if (dbItem.isLoading()) {
                cancelTableLoad(dbItem.getValue());
                dbItem.markUnloaded();
            }
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

            cancelTableOpen();

            LoadingManager.start();
            AsyncQuery<org.canopydb.models.TableSession> query = tableActionService.loadTableDataAsync(
                    selectedItem.getValue(),
                    selectedItem.getParent().getValue()
            );
            inFlightTableOpen = query;

            query.future()
                    .whenComplete((session, error) -> {
                        LoadingManager.stop();
                        if (inFlightTableOpen == query) {
                            inFlightTableOpen = null;
                        }
                    })
                    .thenAccept(session -> Platform.runLater(() ->
                            tableDataAppendAction.render(session)
                    ))
                    .exceptionally(error -> {
                        if (QueryExceptions.isCancellation(error)) {
                            return null;
                        }
                        Platform.runLater(() -> NotificationManager.pushNotification(
                                "Failed to fetch table !",
                                ExceptionMessages.userMessage(error),
                                NotificationManager.NotificationType.DANGER
                        ));
                        return null;
                    });
        }
    }

    private void cancelTableLoad(String database) {
        AsyncQuery<List<String>> existing = inFlightTableLoads.remove(database);
        if (existing != null) {
            existing.cancel();
        }
    }

    private void cancelTableOpen() {
        if (inFlightTableOpen != null) {
            inFlightTableOpen.cancel();
            inFlightTableOpen = null;
        }
    }
}
