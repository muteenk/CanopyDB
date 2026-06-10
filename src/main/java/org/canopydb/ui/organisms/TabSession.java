package org.canopydb.ui.organisms;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.canopydb.models.TableData;
import org.canopydb.models.TableSession;

import java.util.List;

public class TabSession {
    private TableView<List<String>> tableView;

    public TabSession(TableSession tableSession) {
        tableView = buildTableView(tableSession.getTableData());
    }

    private TableView<List<String>> buildTableView(TableData table){
        TableView<List<String>> tableView = new TableView<>();
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
        return tableView;
    }
}
