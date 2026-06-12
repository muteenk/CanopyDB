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
import org.canopydb.ui.atoms.TextInput;


public class TabFilterArea {
    private final VBox filterArea = new VBox();
    private final Button addNewFilter = new Button("Add Filter");
    private final Button clearAllFilters = new Button("Clear Filters");

    private final int MAX_INPUT_COUNT = 10;

    public TabFilterArea(TableSession tableSession, TableViewEventController tableViewEventController) {
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
    }

    private HBox buildFilterInput() {
        TextField filterInput = new TextInput("Enter filter query").getTextField();
        Button filterToggle = new Button("Apply");
        Button removeFilter = new Button(" – ");
        HBox filterBox = new HBox(filterInput, filterToggle, removeFilter);

        filterToggle.getStyleClass().add("filter-apply-button");
        removeFilter.getStyleClass().add("filter-apply-button");
        filterInput.getStyleClass().add("filter-input");
        filterBox.getStyleClass().add("filter-row");
        HBox.setHgrow(filterInput, Priority.ALWAYS);

        filterInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                filterToggle.setText("Applied");
            }
        });
        filterToggle.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            filterToggle.setText("Applied");
        });
        removeFilter.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            removeFilterInput(filterBox);
        });

        return filterBox;
    }

    private void addFilterInput() {
        int currentSize = this.filterArea.getChildren().size();
        if (currentSize > MAX_INPUT_COUNT) return;
        filterArea.getChildren().addFirst(buildFilterInput());
        this.addNewFilter.setDisable(currentSize == MAX_INPUT_COUNT);
        this.clearAllFilters.setDisable(false);
    }

    private void removeFilterInput(HBox filterBox){
        filterArea.getChildren().remove(filterBox);
        this.addNewFilter.setDisable(false);
        this.clearAllFilters.setDisable(this.filterArea.getChildren().size() == 1);
    }

    private void clearAllFilterInputs() {
        filterArea.getChildren().remove(0, this.filterArea.getChildren().size()-1);
        this.addNewFilter.setDisable(false);
        this.clearAllFilters.setDisable(true);
    }

    public VBox getFilterArea() {return this.filterArea;}
}
