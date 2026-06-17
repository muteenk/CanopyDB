package org.canopydb.ui.molecules;

import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableSession;
import org.canopydb.profile.Performance;
import org.canopydb.ui.atoms.FilterBox;
import org.canopydb.utils.Constants;


public class TabFilterArea {
    private final VBox filterMain = new VBox(); // parent container
    private final VBox filterArea = new VBox(); // container for input boxes

    // Filter Controls
    private final Button addNewFilter = new Button("Add Filter");
    private final Button clearAllFilters = new Button("Clear Filters");

    private final TableSession tableSession;
    private final TableViewEventController tableViewEventController;


    public TabFilterArea(TableSession tableSession, TableViewEventController tableViewEventController) {
        this.tableSession = tableSession;
        this.tableViewEventController = tableViewEventController;

        addNewFilter.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> addFilterInput());
        clearAllFilters.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> clearAllFilterInputs());
        clearAllFilters.setDisable(true);

        HBox controls = new HBox(
                addNewFilter,
                clearAllFilters
        );
        filterArea.getChildren().add(controls);
        filterMain.getChildren().addAll(filterArea, controls);

        filterArea.getStyleClass().add("filter-area");
        controls.getStyleClass().add("filter-toolbar");
    }

    private void addFilterInput() {     // Inserts a new filter box to the filter area
        int currentSize = this.filterArea.getChildren().size();
        int MAX_INPUT_COUNT = 10;
        if (currentSize >= MAX_INPUT_COUNT) return;
        filterArea.getChildren().addFirst(buildFilterInput().getFilterBox());
        Performance.logMemory();
        this.addNewFilter.setDisable(currentSize == MAX_INPUT_COUNT -1);
        this.clearAllFilters.setDisable(false);
    }

    private FilterBox buildFilterInput() {      // Builds filter box
        FilterBox filterBox = new FilterBox();

        filterBox.getFilterInput().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {     // to handle enter event on input bar
                event.consume();
                applyFilter(filterBox);
            } else {
                if (filterBox.getFilterToggle().getText().equals(Constants.APPLIED))
                    unapplyFilter(filterBox);
            }
        });

        // apply button handler
        filterBox.getFilterToggle().addEventHandler(
                MouseEvent.MOUSE_CLICKED, _ -> toggleFilter(filterBox)
        );

        // remove filter completely
        filterBox.getRemoveFilter().addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            String input = filterBox.getFilterInput().getText();
            if (!input.isEmpty()) unapplyFilter(filterBox);
            removeFilterInput(filterBox);
        });

        return filterBox;
    }

    private void toggleFilter(FilterBox filterBox){
        String input = filterBox.getFilterInput().getText();
        if (input.isEmpty()) return;

        String filterState = filterBox.getFilterToggle().getText();
        if (filterState.equals(Constants.APPLY)) applyFilter(filterBox);
        else unapplyFilter(filterBox);
    }

    private void applyFilter(FilterBox filterBox) {
        if (filterBox.getFilterToggle().getText().equals(Constants.APPLIED)) return;
        String input = filterBox.getFilterInput().getText();
        if (input.isEmpty()) return;

        filterBox.getFilterToggle().setText(Constants.APPLIED);
        tableSession.getTableQuery().addFilter(filterBox.getFilterID(), input);
        tableViewEventController.tableReRender(tableSession);
    }

    private void unapplyFilter(FilterBox filterBox) {
        if (filterBox.getFilterToggle().getText().equals(Constants.APPLY)) return;

        filterBox.getFilterToggle().setText(Constants.APPLY);
        tableSession.getTableQuery().removeFilter(filterBox.getFilterID());
        tableViewEventController.tableReRender(tableSession);
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
