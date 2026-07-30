package org.canopydb.ui.organisms.workspace;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;
import org.canopydb.controllers.TreeViewEventController;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.ui.utils.TreeSearch;
import org.canopydb.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns sidebar tree search UI and filter/restore of the connection tree.
 * Keeps a canonical snapshot of loaded DB nodes and swaps in a filtered projection while searching.
 */
public class ConnectionTreeSearch {

    private static final Duration SEARCH_DEBOUNCE = Duration.millis(180);

    private final TreeItem<String> connectionRoot;
    private final TreeViewEventController treeViewEventController;

    private final List<TreeItem<String>> canonicalDatabases = new ArrayList<>();
    private final TextField searchField = new TextInput("Search databases & tables").getTextField();
    private final Label emptySearchLabel = new Label();
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);

    private String currentQuery = "";
    private boolean filtering;

    public ConnectionTreeSearch(
            TreeItem<String> connectionRoot,
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
        canonicalDatabases.clear();
        if (connectionRoot.getChildren().isEmpty()) {
            return;
        }
        String first = connectionRoot.getChildren().getFirst().getValue();
        if (Constants.LOADING.equals(first) || Constants.FAILED.equals(first)) {
            return;
        }
        // Only capture when displaying the canonical tree (not a filtered projection).
        if (!filtering) {
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
                // Nothing loaded yet — keep the live tree (LOADING / etc.) intact.
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
        TreeItem<String> projected = new TreeItem<>(canonicalDb.getValue());

        for (TreeItem<String> table : match.tables()) {
            projected.getChildren().add(new TreeItem<>(table.getValue()));
        }

        // DB name matched but tables not loaded yet — allow expand to load, then re-filter.
        if (match.tables().isEmpty() && !TreeSearch.hasLoadedTables(canonicalDb)) {
            String first = canonicalDb.getChildren().isEmpty()
                    ? null
                    : canonicalDb.getChildren().getFirst().getValue();
            if (Constants.LOADING.equals(first) || canonicalDb.getChildren().isEmpty()) {
                projected.getChildren().add(new TreeItem<>(Constants.LOADING));
                projected.addEventHandler(TreeItem.<String>branchExpandedEvent(), event -> {
                    if (event.getSource() != projected) return;
                    treeViewEventController.dbExpandHandler(canonicalDb);
                });
            }
        } else {
            projected.setExpanded(true);
        }

        return projected;
    }

    private void restoreCanonicalTree() {
        filtering = false;
        if (canonicalDatabases.isEmpty()) {
            refreshCanonicalDatabases();
        }
        if (!canonicalDatabases.isEmpty()) {
            connectionRoot.getChildren().setAll(canonicalDatabases);
        }
    }

    private void setEmptySearchVisible(boolean visible, String message) {
        emptySearchLabel.setText(message == null ? "" : message);
        emptySearchLabel.setVisible(visible);
        emptySearchLabel.setManaged(visible);
    }
}
