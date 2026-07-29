package org.canopydb.ui.atoms;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.canopydb.utils.Constants;

import java.util.UUID;

public class FilterBox {
    private final TextField filterInput = new TextInput("Enter filter query").getTextField();
    private final Button filterToggle = new Button(Constants.APPLY);
    private final Button removeFilter = new Button(" – ");
    private final HBox filterBox = new HBox(8);

    private boolean appliedState = false;
    private final String filterID;

    public FilterBox() {
        this.filterID = UUID.randomUUID().toString();

        filterToggle.getStyleClass().add("filter-apply-button");
        removeFilter.getStyleClass().add("filter-apply-button");
        filterInput.getStyleClass().add("filter-input");
        filterBox.getStyleClass().add("filter-row");
        filterBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(filterInput, Priority.ALWAYS);

        filterBox.getChildren().addAll(filterInput, filterToggle, removeFilter);
    }

    public String getFilterID() {return this.filterID;}

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

    public boolean isApplied() {return this.appliedState;}
    public void setApplied(boolean state){
        this.appliedState = state;
        this.filterToggle.setText((this.appliedState) ? Constants.APPLIED : Constants.APPLY);
    }
}
