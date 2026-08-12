package org.canopydb.ui.molecules;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableSession;
import org.canopydb.config.Profiler;
import org.canopydb.ui.atoms.FilterBox;
import org.canopydb.ui.atoms.IconButton;


public class TabFilterArea {
    private static final int MAX_INPUT_COUNT = 10;

    private final VBox filterMain = new VBox();
    private final VBox filterRows = new VBox();

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

        Button refreshTable = IconButton.refresh("Refresh Table");
        refreshTable.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> tableViewEventController.tableReRender(tableSession));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox controls = new HBox(8, addNewFilter, clearAllFilters, spacer, refreshTable);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getStyleClass().add("filter-toolbar");

        filterRows.getStyleClass().add("filter-rows");
        filterRows.setFillWidth(true);

        filterMain.getStyleClass().add("filter-area");
        filterMain.getChildren().addAll(controls, filterRows);
        VBox.setVgrow(filterRows, Priority.NEVER);
    }

    private void addFilterInput() {
        int currentSize = filterRows.getChildren().size();
        if (currentSize >= MAX_INPUT_COUNT) return;

        filterRows.getChildren().add(buildFilterInput().getFilterBox());
        Profiler.logMemory();
        addNewFilter.setDisable(currentSize + 1 >= MAX_INPUT_COUNT);
        clearAllFilters.setDisable(false);
    }

    private FilterBox buildFilterInput() {
        FilterBox filterBox = new FilterBox();

        filterBox.getFilterInput().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                applyFilter(filterBox);
            }
        });

        filterBox.getFilterInput()
                .textProperty()
                .addListener((obs, oldValue, newValue) -> {
                    if (filterBox.isApplied()) {
                        unapplyFilter(filterBox);
                    }
                });

        filterBox.getFilterToggle().addEventHandler(
                MouseEvent.MOUSE_CLICKED, _ -> toggleFilter(filterBox)
        );

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

        if (!filterBox.isApplied()) applyFilter(filterBox);
        else unapplyFilter(filterBox);
    }

    private void applyFilter(FilterBox filterBox) {
        if (filterBox.isApplied()) return;
        String input = filterBox.getFilterInput().getText();
        if (input.isEmpty()) return;

        filterBox.setApplied(true);
        tableSession.addQueryFilter(filterBox.getFilterID(), input);
        tableViewEventController.tableReRender(tableSession);
    }

    private void unapplyFilter(FilterBox filterBox) {
        if (!filterBox.isApplied()) return;

        filterBox.setApplied(false);
        tableSession.removeQueryFilter(filterBox.getFilterID());
        tableViewEventController.tableReRender(tableSession);
    }

    private void removeFilterInput(FilterBox filterBox){
        filterRows.getChildren().remove(filterBox.getFilterBox());
        addNewFilter.setDisable(false);
        clearAllFilters.setDisable(filterRows.getChildren().isEmpty());
    }

    private void clearAllFilterInputs() {
        filterRows.getChildren().clear();
        tableSession.clearQueryFilters();
        tableViewEventController.tableReRender(tableSession);
        addNewFilter.setDisable(false);
        clearAllFilters.setDisable(true);
    }

    public VBox getFilterArea() {
        return filterMain;
    }
}
