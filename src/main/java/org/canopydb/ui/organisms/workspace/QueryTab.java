package org.canopydb.ui.organisms.workspace;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.QueryEditorController;
import org.canopydb.models.CellValue;
import org.canopydb.models.QueryResult;
import org.canopydb.models.QuerySession;
import org.canopydb.ui.utils.TableComponent;

import java.util.List;

/**
 * Ad-hoc SQL editor tab with Run / Cancel and a results pane.
 */
public class QueryTab {

    private final Tab tab;
    private final QuerySession session;
    private final TextArea editor;
    private final Button runButton;
    private final Button cancelButton;
    private final Label statusLabel;
    private final StackPane resultsHost;
    private final Label resultsPlaceholder;
    private final QueryEditorController controller;

    public QueryTab(QuerySession session) {
        this.session = session;
        this.controller = new QueryEditorController(
                this::showResult,
                this::showFailure,
                this::showCancelled,
                this::updateBusyState
        );

        editor = new TextArea();
        editor.setPromptText("Write SQL here…");
        editor.getStyleClass().add("query-editor");
        editor.setWrapText(false);
        editor.textProperty().addListener((obs, oldVal, newVal) -> session.setSql(newVal));
        editor.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEditorShortcut);

        runButton = new Button("Run");
        runButton.getStyleClass().addAll("query-toolbar-button", "query-toolbar-button-primary");
        runButton.setOnAction(e -> run());

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("query-status-label");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("query-toolbar-button");
        cancelButton.setDisable(true);
        cancelButton.setOnAction(e -> controller.cancel());

        HBox toolbar = new HBox(8, runButton, cancelButton, statusLabel);
        toolbar.getStyleClass().add("query-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        resultsPlaceholder = new Label("Run a query to see results here.");
        resultsPlaceholder.getStyleClass().add("query-results-placeholder");

        resultsHost = new StackPane(resultsPlaceholder);
        resultsHost.getStyleClass().add("query-results-pane");

        VBox editorPane = new VBox(editor);
        editorPane.getStyleClass().add("query-editor-pane");
        VBox.setVgrow(editor, Priority.ALWAYS);

        SplitPane split = new SplitPane(editorPane, resultsHost);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.45);
        split.getStyleClass().add("query-split");
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox root = new VBox(toolbar, split);
        root.getStyleClass().add("query-tab");
        root.setFillWidth(true);

        tab = new Tab(session.getTitle(), root);
        tab.getStyleClass().add("query-editor-tab");
    }

    public Tab getTab() {
        return tab;
    }

    public QuerySession getSession() {
        return session;
    }

    public void cancelPending() {
        controller.cancel();
    }

    public void dispose() {
        controller.cancel();
        editor.clear();
        resultsHost.getChildren().clear();
        session.dispose();
        tab.setContent(null);
        tab.setOnClosed(null);
    }

    private void run() {
        String selected = editor.getSelectedText();
        String sql = (selected != null && !selected.isBlank())
                ? selected
                : editor.getText();
        controller.run(sql);
    }

    private void handleEditorShortcut(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && event.isShortcutDown()) {
            event.consume();
            if (!controller.isRunning()) {
                run();
            }
        }
    }

    private void updateBusyState() {
        boolean running = controller.isRunning();
        runButton.setDisable(running);
        cancelButton.setDisable(!running);
        editor.setEditable(!running);
        if (running) {
            statusLabel.setText("Running…");
        }
    }

    private void showCancelled() {
        statusLabel.setText("Cancelled");
        resultsHost.getChildren().clear();
        Label message = new Label("Cancelled");
        message.getStyleClass().add("query-results-placeholder");
        resultsHost.getChildren().add(message);
    }

    private void showFailure(String message) {
        statusLabel.setText("Failed");
        resultsHost.getChildren().clear();
        Label error = new Label(message == null ? "Query failed" : message);
        error.getStyleClass().add("query-results-message");
        error.setWrapText(true);
        resultsHost.getChildren().add(error);
    }

    private void showResult(QueryResult result) {
        statusLabel.setText(result.statusMessage());
        resultsHost.getChildren().clear();

        if (result.hasResultSet() && result.getTableData() != null) {
            TableView<List<CellValue>> tableView = TableComponent.buildTableComponent(result.getTableData());
            tableView.getStyleClass().add("query-results-table");
            StackPane.setAlignment(tableView, Pos.TOP_LEFT);
            resultsHost.getChildren().add(tableView);
        } else {
            Label message = new Label(result.statusMessage());
            message.getStyleClass().add("query-results-message");
            resultsHost.getChildren().add(message);
        }
    }
}
