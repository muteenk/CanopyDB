package org.canopydb.ui.organisms;

import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.ui.molecules.TableComponent;

import java.util.List;

public class MiddlePane {
    private final VBox middleArea = new VBox();
    public VBox getMiddle() {
        return middleArea;
    }

    public void addTable(List<List<String>> tableData) {
        TableView<List<String>> tableView = TableComponent.buildTable(tableData);
        middleArea.getChildren().clear();
        middleArea.getChildren().add(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }
}
