package org.canopydb.ui.organisms.workspace;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.utils.LazyTreeItem;
import org.canopydb.ui.utils.TreeSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns sidebar tree search UI and filter/restore of the connection tree.
 * Keeps a canonical snapshot of loaded DB nodes and swaps in a filtered projection while searching.
 */
public class ConnectionTreeSearch {

    private static final Duration SEARCH_DEBOUNCE = Duration.millis(180);

    private final LazyTreeItem connectionRoot;
    private final TreeViewEventController treeViewEventController;

    private final List<TreeItem<String>> canonicalDatabases = new ArrayList<>();
    private final TextField searchField = new TextInput("Search databases & tables").getTextField();
    private final Label emptySearchLabel = new Label();
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);

    private String currentQuery = "";
    private boolean filtering;

    public ConnectionTreeSearch(
            LazyTreeItem connectionRoot,
            TreeViewEventController treeViewEventController
    ) {
        this.connectionRoot = connectionRoot;
        this.treeViewEventController = treeViewEventController;
        configureSearchUi();
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Label getEmptySearchLabel() {
        return emptySearchLabel;
    }

    /** Call when DB list or table list finishes loading. */
    public void onTreeDataChanged() {
        refreshCanonicalDatabases();
        if (!TreeSearch.normalizeQuery(currentQuery).isEmpty()) {
            applySearch(currentQuery);
        }
    }

    private void configureSearchUi() {
        searchField.getStyleClass().add("sidebar-search");
        searchDebounce.setOnFinished(e -> applySearch(searchField.getText()));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.stop();
            searchDebounce.playFromStart();
        });

        emptySearchLabel.getStyleClass().add("sidebar-search-empty");
        emptySearchLabel.setVisible(false);
        emptySearchLabel.setManaged(false);
        emptySearchLabel.setWrapText(true);
        emptySearchLabel.setMaxWidth(Double.MAX_VALUE);
        emptySearchLabel.setAlignment(Pos.CENTER_LEFT);
    }

    private void refreshCanonicalDatabases() {
        if (!connectionRoot.isLoaded()) {
            if (!filtering) {
                canonicalDatabases.clear();
            }
            return;
        }
        // While filtering, canonical DB nodes are updated in place by the controller.
        if (!filtering) {
            canonicalDatabases.clear();
            canonicalDatabases.addAll(connectionRoot.getChildren());
        }
    }

    private void applySearch(String query) {
        currentQuery = query == null ? "" : query;
        String normalized = TreeSearch.normalizeQuery(currentQuery);

        if (normalized.isEmpty()) {
            restoreCanonicalTree();
            setEmptySearchVisible(false, null);
            return;
        }

        ensureCanonicalSnapshot();
        List<TreeSearch.Match> matches = TreeSearch.findLoadedMatches(canonicalDatabases, normalized);

        if (matches.isEmpty()) {
            if (canonicalDatabases.isEmpty()) {
                filtering = false;
                setEmptySearchVisible(
                        true,
                        "Expand Connection and a database to search loaded items."
                );
                return;
            }

            filtering = true;
            connectionRoot.getChildren().clear();
            boolean anyLoadedTables = canonicalDatabases.stream().anyMatch(TreeSearch::hasLoadedTables);
            String message = anyLoadedTables
                    ? "No loaded databases or tables match \"" + currentQuery.trim() + "\"."
                    : "No database names match. Expand a database to search its tables.";
            setEmptySearchVisible(true, message);
            return;
        }

        setEmptySearchVisible(false, null);
        filtering = true;
        List<TreeItem<String>> projected = new ArrayList<>();
        for (TreeSearch.Match match : matches) {
            projected.add(buildProjectedDatabase(match));
        }
        connectionRoot.getChildren().setAll(projected);
        connectionRoot.setExpanded(true);
    }

    private void ensureCanonicalSnapshot() {
        if (!filtering && canonicalDatabases.isEmpty()) {
            refreshCanonicalDatabases();
        }
    }

    private TreeItem<String> buildProjectedDatabase(TreeSearch.Match match) {
        TreeItem<String> canonicalDb = match.database();
        LazyTreeItem projected = new LazyTreeItem(canonicalDb.getValue());

        for (TreeItem<String> table : match.tables()) {
            projected.getChildren().add(new TreeItem<>(table.getValue()));
        }

        if (!match.tables().isEmpty() || TreeSearch.hasLoadedTables(canonicalDb)
                || (canonicalDb instanceof LazyTreeItem lazy && lazy.isLoaded())) {
            projected.markLoaded();
            if (!match.tables().isEmpty()) {
                projected.setExpanded(true);
            }
            return projected;
        }

        // DB name matched but tables not loaded yet — expand loads the canonical node.
        projected.addEventHandler(TreeItem.<String>branchExpandedEvent(), event -> {
            if (event.getSource() != projected) {
                return;
            }
            if (canonicalDb instanceof LazyTreeItem lazyCanonical) {
                treeViewEventController.dbExpandHandler(lazyCanonical, projected);
            }
        });

        return projected;
    }

    private void restoreCanonicalTree() {
        filtering = false;
        if (canonicalDatabases.isEmpty()) {
            refreshCanonicalDatabases();
        }
        if (!canonicalDatabases.isEmpty()) {
            connectionRoot.getChildren().setAll(canonicalDatabases);
            if (!connectionRoot.isLoaded()) {
                connectionRoot.markLoaded();
            }
        }
    }

    private void setEmptySearchVisible(boolean visible, String message) {
        emptySearchLabel.setText(message == null ? "" : message);
        emptySearchLabel.setVisible(visible);
        emptySearchLabel.setManaged(visible);
    }
}
