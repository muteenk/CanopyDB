package org.canopydb.ui.organisms;

import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class MiddlePane {
    private final VBox middleArea = new VBox();
    public VBox getMiddle() {
//        VBox.setVgrow(middleArea, Priority.ALWAYS);
        return middleArea;
    }

    public void addTable(TableView<List<String>> tableView) {
        middleArea.getChildren().add(tableView);
    }
}
