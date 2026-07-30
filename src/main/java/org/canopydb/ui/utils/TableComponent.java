package org.canopydb.ui.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.canopydb.config.AppLogger;
import org.canopydb.models.CellValue;
import org.canopydb.models.TableData;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TableComponent {
    private static final Logger LOGGER = AppLogger.getLogger(TableComponent.class);

    /** Default visible width; large cell text will ellipsize instead of stretching the column. */
    private static final double COLUMN_PREF_WIDTH = 180;
    private static final double COLUMN_MIN_WIDTH = 64;

    private TableComponent() {
    }

    public static TableView<List<CellValue>> buildTableComponent(TableData table) {
        TableView<List<CellValue>> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        List<String> tableHeaders = table.getHeaders();
        for (int i = 0; i < tableHeaders.size(); i++) {
            final int columnIndex = i;
            TableColumn<List<CellValue>, String> column = new TableColumn<>(tableHeaders.get(i));
            column.setMinWidth(COLUMN_MIN_WIDTH);
            column.setPrefWidth(COLUMN_PREF_WIDTH);
            // Keep max open so users can drag columns wider than the default on purpose.
            column.setMaxWidth(Double.MAX_VALUE);
            column.setResizable(true);
            column.setCellValueFactory(cellData -> {
                List<CellValue> row = cellData.getValue();
                String cellValue = (columnIndex < row.size())
                        ? row.get(columnIndex).toDisplayString()
                        : "";
                return new SimpleStringProperty(cellValue);
            });
            tableView.getColumns().add(column);
        }

        ObservableList<List<CellValue>> observableRows =
                FXCollections.observableArrayList(table.getRows());
        tableView.setItems(observableRows);
        return tableView;
    }

    public static void debugLogTableData(TableData table) {
        LOGGER.fine("------- HEADERS --------");
        LOGGER.fine(String.join("  ", table.getHeaders()));

        LOGGER.fine("------- ROWS ---------");
        for (List<CellValue> row : table.getRows()) {
            LOGGER.fine(row.stream()
                    .map(CellValue::toDisplayString)
                    .collect(Collectors.joining("  ")));
        }
    }

    public static void updateTableContents(TableData table, TableView<List<CellValue>> tableView) {
        ObservableList<List<CellValue>> observableRows =
                FXCollections.observableArrayList(table.getRows());
        tableView.setItems(observableRows);
    }
}
