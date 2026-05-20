package org.canopydb.ui.molecules;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.canopydb.entities.TableData;

import java.util.List;

public class TableComponent {
    TableView<List<String>> tableView;

    public TableComponent(TableData table){
        tableView = new TableView<>();
        List<String> tableHeaders = table.getHeaders();
        for (int i = 0; i < tableHeaders.size(); i++) {
            final int columnIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(tableHeaders.get(i));
            column.setCellValueFactory(cellData -> {
                List<String> row = cellData.getValue();
                String cellValue = (columnIndex < row.size()) ? row.get(columnIndex) : "";
                return new SimpleStringProperty(cellValue);
            });
            tableView.getColumns().add(column);
        }

        ObservableList<List<String>> observableRows = FXCollections.observableArrayList(table.getRows());
        tableView.setItems(observableRows);
    }

    public static void debugLogTableData(TableData table) {
        System.out.println("------- HEADERS --------");
        for (String col: table.getHeaders()){
            System.out.print(col + "  ");
        }
        System.out.println("------- ROWS ---------");
        for (List<String> row: table.getRows()){
            for (String item: row){
                System.out.print(item + "  ");
            }
            System.out.println(" ");
        }
    }

    public TableView<List<String>> getTable() {
        return tableView;
    }
}
