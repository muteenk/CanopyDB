package org.canopydb.ui.organisms.workspace;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.models.QuerySession;

/**
 * Ad-hoc SQL editor tab. Execution comes later — this shell opens from the sidebar.
 */
public class QueryTab {

    private final Tab tab;
    private final QuerySession session;
    private final TextArea editor;

    public QueryTab(QuerySession session) {
        this.session = session;
        this.editor = new TextArea();
        this.editor.setPromptText("Write SQL here…");
        this.editor.getStyleClass().add("query-editor");
        this.editor.setWrapText(false);
        this.editor.textProperty().addListener((obs, oldVal, newVal) ->
                session.setSql(newVal)
        );

        Label resultsPlaceholder = new Label("Run a query to see results here.");
        resultsPlaceholder.getStyleClass().add("query-results-placeholder");

        VBox resultsPane = new VBox(resultsPlaceholder);
        resultsPane.getStyleClass().add("query-results-pane");
        resultsPane.setFillWidth(true);

        SplitPane split = new SplitPane(editor, resultsPane);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.45);
        split.getStyleClass().add("query-split");
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox root = new VBox(split);
        root.getStyleClass().add("query-tab");
        root.setFillWidth(true);
        VBox.setVgrow(split, Priority.ALWAYS);

        tab = new Tab(session.getTitle(), root);
        tab.getStyleClass().add("query-editor-tab");
    }

    public Tab getTab() {
        return tab;
    }

    public QuerySession getSession() {
        return session;
    }

    public void dispose() {
        editor.clear();
        session.dispose();
        tab.setContent(null);
        tab.setOnClosed(null);
    }
}
