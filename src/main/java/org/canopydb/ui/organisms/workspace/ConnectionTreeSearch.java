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
 * Owns sidebar tree search UI and filter/restore of pinned database nodes.
 * Keeps a canonical snapshot and swaps in a filtered projection while searching.
 */
public class ConnectionTreeSearch {

    private static final Duration SEARCH_DEBOUNCE = Duration.millis(180);

    private final TreeItem<String> databasesRoot;
    private final TreeViewEventController treeViewEventController;

    private final List<TreeItem<String>> canonicalDatabases = new ArrayList<>();
    private final TextField searchField = new TextInput("Search databases & tables").getTextField();
    private final Label emptySearchLabel = new Label();
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);

    private String currentQuery = "";
    private boolean filtering;

    public ConnectionTreeSearch(
            TreeItem<String> databasesRoot,
            TreeViewEventController treeViewEventController
    ) {
        this.databasesRoot = databasesRoot;
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
        if (!filtering) {
            canonicalDatabases.clear();
            canonicalDatabases.addAll(databasesRoot.getChildren());
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
                        "Add a database to search its tables."
                );
                return;
            }

            filtering = true;
            databasesRoot.getChildren().clear();
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
        databasesRoot.getChildren().setAll(projected);
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
            databasesRoot.getChildren().setAll(canonicalDatabases);
        }
    }

    private void setEmptySearchVisible(boolean visible, String message) {
        emptySearchLabel.setText(message == null ? "" : message);
        emptySearchLabel.setVisible(visible);
        emptySearchLabel.setManaged(visible);
    }
}
