package org.canopydb.ui.molecules;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableSession;
import org.canopydb.ui.atoms.FilterBox;
import org.canopydb.ui.atoms.TextInput;
import org.canopydb.utils.Constants;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Filter;


public class TabFilterArea {
    private final VBox filterMain = new VBox(); // parent container
    private final VBox filterArea = new VBox(); // container for input boxes

    private final Button addNewFilter = new Button("Add Filter");
    private final Button clearAllFilters = new Button("Clear Filters");

    private final HashMap<String, FilterBox> filters = new HashMap<>();

    private final TableSession tableSession;
    private final TableViewEventController tableViewEventController;

    private final int MAX_INPUT_COUNT = 10;

    public TabFilterArea(TableSession tableSession, TableViewEventController tableViewEventController) {
        this.tableSession = tableSession;
        this.tableViewEventController = tableViewEventController;

        addNewFilter.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            addFilterInput();
        });

        clearAllFilters.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            clearAllFilterInputs();
        });
        clearAllFilters.setDisable(true);

        HBox controls = new HBox(
                addNewFilter,
                clearAllFilters
        );
        filterArea.getChildren().add(controls);

        filterArea.getStyleClass().add("filter-area");
        controls.getStyleClass().add("filter-toolbar");

        filterMain.getChildren().addAll(filterArea, controls);
    }

    private FilterBox buildFilterInput() {
        UUID uuid = UUID.randomUUID();
        FilterBox filterBox = new FilterBox(uuid.toString());

        filterBox.getFilterInput().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                if (filterBox.getFilterInput().getText().equals(Constants.APPLIED)) return;
                String input = filterBox.getFilterInput().getText();
                if (input.isEmpty()) return;
                filterBox.getFilterToggle().setText(Constants.APPLIED);
                tableSession.getTableQuery().addFilter(uuid.toString(), input);
                tableViewEventController.tableReRender(tableSession);
            }
        });
        filterBox.getFilterToggle().addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            String input = filterBox.getFilterInput().getText();
            if (input.isEmpty()) return;

            String filterState = filterBox.getFilterToggle().getText();
            if (filterState.equals(Constants.APPLY)) {
                filterBox.getFilterToggle().setText(Constants.APPLIED);
                tableSession.getTableQuery().addFilter(uuid.toString(), input);
                tableViewEventController.tableReRender(tableSession);
            } else {
                filterBox.getFilterToggle().setText(Constants.APPLY);
                tableSession.getTableQuery().removeFilter(uuid.toString());
                tableViewEventController.tableReRender(tableSession);
            }
        });
        filterBox.getRemoveFilter().addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            String input = filterBox.getFilterInput().getText();
            if (!input.isEmpty()) {
                tableSession.getTableQuery().removeFilter(uuid.toString());
                tableViewEventController.tableReRender(tableSession);
            }
            removeFilterInput(filterBox);
        });

        return filterBox;
    }

    private void addFilterInput() {
        int currentSize = this.filterArea.getChildren().size();
        if (currentSize >= MAX_INPUT_COUNT) return;
        filterArea.getChildren().addFirst(buildFilterInput().getFilterBox());
        this.addNewFilter.setDisable(currentSize == MAX_INPUT_COUNT-1);
        this.clearAllFilters.setDisable(false);
    }

    private void removeFilterInput(FilterBox filterBox){
        filterArea.getChildren().remove(filterBox.getFilterBox());
        this.addNewFilter.setDisable(false);
        this.clearAllFilters.setDisable(this.filterArea.getChildren().isEmpty());
    }

    private void clearAllFilterInputs() {
        filterArea.getChildren().clear();
        tableSession.getTableQuery().clearFilter();
        tableViewEventController.tableReRender(tableSession);
        this.addNewFilter.setDisable(false);
        this.clearAllFilters.setDisable(true);
    }

    public VBox getFilterArea() {return this.filterMain;}
}
