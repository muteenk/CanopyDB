package org.canopydb.ui.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.canopydb.models.TableData;

import java.util.List;
import java.util.logging.Logger;
import org.canopydb.config.AppLogger;

public class TableComponent {
    private static final Logger LOGGER = AppLogger.getLogger(TableComponent.class);

    private TableComponent() {
    }

    public static TableView<List<String>> buildTableComponent(TableData table){
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

    public static void debugLogTableData(TableData table) {
        // Keeing this at FINE so it doesn't spam normal users.
        LOGGER.fine("------- HEADERS --------");
        LOGGER.fine(String.join("  ", table.getHeaders()));

        LOGGER.fine("------- ROWS ---------");
        for (List<String> row : table.getRows()) {
            LOGGER.fine(String.join("  ", row));
        }
    }

    public static void updateTableContents(TableData table, TableView<List<String>> tableView){
        ObservableList<List<String>> observableRows = FXCollections.observableArrayList(table.getRows());
        tableView.setItems(observableRows);
    }
}
