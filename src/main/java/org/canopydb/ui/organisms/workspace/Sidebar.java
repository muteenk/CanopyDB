package org.canopydb.ui.organisms.workspace;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.canopydb.config.Profiler;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.atoms.IconButton;
import org.canopydb.ui.interfaces.TableActiveCheck;
import org.canopydb.ui.interfaces.TableOpenAction;
import org.canopydb.ui.singletons.LoadingManager;
import org.canopydb.ui.singletons.NotificationManager;
import org.canopydb.ui.utils.DialogStyles;
import org.canopydb.ui.utils.LazyTreeItem;
import org.canopydb.utils.ExceptionMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Workspace left pane: pinned databases + search.
 * Only databases the user adds appear in the tree; expand one to load its tables.
 */
public class Sidebar {

    public static final double MIN_WIDTH = 180;
    public static final double PREF_WIDTH = 260;
    public static final double MAX_WIDTH = 480;

    private final TreeViewEventController treeViewEventController;
    /** Hidden root; pinned {@link LazyTreeItem} databases are direct children. */
    private final TreeItem<String> databasesRoot = new TreeItem<>();
    private final ConnectionTreeSearch treeSearch;
    private TreeView<String> databaseTreeView;

    public Sidebar(TableOpenAction tableOpenAction, TableActiveCheck tableActiveCheck) {
        treeViewEventController = new TreeViewEventController(tableOpenAction, tableActiveCheck);
        treeSearch = new ConnectionTreeSearch(databasesRoot, treeViewEventController);
        treeViewEventController.setOnTreeDataChanged(treeSearch::onTreeDataChanged);
    }

    public VBox getSidebar() {
        databaseTreeView = buildTreeView();

        Label databasesLabel = new Label("Databases");
        databasesLabel.getStyleClass().add("sidebar-section-label");

        Button refreshButton = IconButton.refresh("Refresh loaded tables");
        refreshButton.setOnAction(e -> treeViewEventController.refreshPinnedDatabases(databasesRoot));

        Button addDatabaseButton = IconButton.plus("Add database");
        addDatabaseButton.setOnAction(e -> showAddDatabaseDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbarActions = new HBox(6, refreshButton, addDatabaseButton);
        toolbarActions.getStyleClass().add("sidebar-toolbar-actions");
        toolbarActions.setAlignment(Pos.CENTER_RIGHT);

        HBox databaseToolbar = new HBox(databasesLabel, spacer, toolbarActions);
        databaseToolbar.getStyleClass().add("sidebar-database-toolbar");
        databaseToolbar.setAlignment(Pos.CENTER_LEFT);

        VBox sidebar = new VBox(
                8,
                treeSearch.getSearchField(),
                treeSearch.getEmptySearchLabel(),
                databaseToolbar,
                databaseTreeView
        );
        sidebar.getStyleClass().add("sidebar");
        sidebar.setFillWidth(true);
        sidebar.setMinWidth(MIN_WIDTH);
        sidebar.setPrefWidth(PREF_WIDTH);
        sidebar.setMaxWidth(MAX_WIDTH);
        VBox.setVgrow(databaseTreeView, Priority.ALWAYS);

        return sidebar;
    }

    private TreeView<String> buildTreeView() {
        TreeView<String> treeView = new TreeView<>(databasesRoot);
        treeView.setShowRoot(false);
        treeView.setOnMouseClicked(
                event -> {
                    if (event.getClickCount() == 2) {
                        TreeItem<String> selectedItem = treeView
                                .getSelectionModel().getSelectedItem();
                        treeViewEventController.nodeClickEventHandler(selectedItem);
                        Profiler.logMemory();
                    }
                }
        );
        return treeView;
    }

    private void showAddDatabaseDialog() {
        LoadingManager.start();
        treeViewEventController.fetchAllDatabasesAsync()
                .whenComplete((databases, error) -> LoadingManager.stop())
                .thenAccept(allDatabases -> Platform.runLater(() -> {
                    if (allDatabases == null || allDatabases.isEmpty()) {
                        NotificationManager.pushNotification(
                                "No databases found",
                                "The connected server returned no databases.",
                                NotificationManager.NotificationType.INFO
                        );
                        return;
                    }

                    Set<String> pinned = databasesRoot.getChildren().stream()
                            .map(TreeItem::getValue)
                            .collect(Collectors.toSet());

                    List<String> available = new ArrayList<>();
                    for (String name : allDatabases) {
                        if (!pinned.contains(name)) {
                            available.add(name);
                        }
                    }

                    if (available.isEmpty()) {
                        NotificationManager.pushNotification(
                                "All databases added",
                                "Every database from this connection is already in the sidebar.",
                                NotificationManager.NotificationType.INFO
                        );
                        return;
                    }

                    Scene scene = databaseTreeView != null ? databaseTreeView.getScene() : null;

                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Add Database");
                    DialogStyles.apply(dialog, scene);

                    Label title = new Label("Add Database");
                    title.getStyleClass().add("canopy-dialog-title");

                    Label description = new Label("Choose a database to pin in the sidebar");
                    description.getStyleClass().add("canopy-dialog-description");
                    description.setWrapText(true);

                    ComboBox<String> selector = new ComboBox<>(
                            FXCollections.observableArrayList(available)
                    );
                    selector.getSelectionModel().selectFirst();
                    selector.setMaxWidth(Double.MAX_VALUE);
                    selector.getStyleClass().add("canopy-dialog-combo");

                    VBox content = new VBox(8, title, description, selector);
                    content.setFillWidth(true);
                    content.getStyleClass().add("canopy-dialog-body");
                    dialog.getDialogPane().setContent(content);

                    ButtonType add = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    dialog.getDialogPane().getButtonTypes().addAll(add, cancel);
                    dialog.setResultConverter(button -> button == add ? selector.getValue() : null);

                    dialog.setOnShowing(event -> {
                        var pane = dialog.getDialogPane();
                        pane.lookupButton(add).getStyleClass().add("canopy-dialog-button-primary");
                        pane.lookupButton(cancel).getStyleClass().add("canopy-dialog-button");
                    });

                    Optional<String> chosen = dialog.showAndWait();
                    chosen.ifPresent(name -> {
                        LazyTreeItem added = treeViewEventController.addPinnedDatabase(databasesRoot, name);
                        if (added == null) {
                            NotificationManager.pushNotification(
                                    "Already added",
                                    name + " is already in the sidebar.",
                                    NotificationManager.NotificationType.INFO
                            );
                        }
                    });
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> NotificationManager.pushNotification(
                            "Failed to fetch databases",
                            ExceptionMessages.userMessage(error),
                            NotificationManager.NotificationType.DANGER
                    ));
                    return null;
                });
    }
}
