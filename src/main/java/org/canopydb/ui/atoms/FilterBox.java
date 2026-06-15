package org.canopydb.ui.atoms;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.canopydb.utils.Constants;

public class FilterBox {
    private final TextField filterInput = new TextInput("Enter filter query").getTextField();
    private final Button filterToggle = new Button(Constants.APPLY);
    private final Button removeFilter = new Button(" – ");
    private final HBox filterBox = new HBox();

    private final String filterID;

    public FilterBox(String filterID) {
        this.filterID = filterID;
        filterToggle.getStyleClass().add("filter-apply-button");
        removeFilter.getStyleClass().add("filter-apply-button");
        filterInput.getStyleClass().add("filter-input");
        filterBox.getStyleClass().add("filter-row");
        HBox.setHgrow(filterInput, Priority.ALWAYS);

        filterBox.getChildren().addAll(filterInput, filterToggle, removeFilter);
    }

    public HBox getFilterBox() {
        return filterBox;
    }

    public TextField getFilterInput() {
        return filterInput;
    }

    public Button getFilterToggle() {
        return filterToggle;
    }

    public Button getRemoveFilter() {
        return removeFilter;
    }
}
