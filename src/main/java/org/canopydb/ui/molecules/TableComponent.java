package org.canopydb.ui.molecules;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class TableComponent {
    public static TableView<List<String>> buildTable(List<List<String>> table) {
        TableView<List<String>> tableView = new TableView<>();
        for (int i = 0; i < table.getFirst().size(); i++) {
            final int columnIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(table.getFirst().get(i));
            column.setCellValueFactory(cellData -> {
                List<String> row = cellData.getValue();
                String cellValue = (columnIndex < row.size()) ? row.get(columnIndex) : "";
                return new SimpleStringProperty(cellValue);
            });
            tableView.getColumns().add(column);
        }

        table.removeFirst();
        if (table.isEmpty()) {
            tableView.setPlaceholder(new javafx.scene.control.Label("Table is empty."));
        }
        ObservableList<List<String>> observableRows = FXCollections.observableArrayList(table);
        tableView.setItems(observableRows);

        return tableView;
    }
}
