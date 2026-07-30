package org.canopydb.ui.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
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
            column.setContextMenu(createHeaderContextMenu(column));
            column.setCellFactory(col -> createCopyableCell(columnIndex));
            tableView.getColumns().add(column);
        }

        ObservableList<List<CellValue>> observableRows =
                FXCollections.observableArrayList(table.getRows());
        tableView.setItems(observableRows);
        return tableView;
    }

    private static ContextMenu createHeaderContextMenu(TableColumn<?, ?> column) {
        MenuItem copyColumnName = new MenuItem("Copy column name");
        copyColumnName.getStyleClass().add("table-context-menu-item");
        copyColumnName.setOnAction(e -> ClipboardUtil.copy(column.getText()));

        ContextMenu contextMenu = new ContextMenu(copyColumnName);
        contextMenu.getStyleClass().add("table-context-menu");
        return contextMenu;
    }

    private static TableCell<List<CellValue>, String> createCopyableCell(int columnIndex) {
        return new TableCell<>() {
            private final MenuItem copyCellItem = new MenuItem("Copy cell value");
            private final MenuItem copyRowItem = new MenuItem("Copy row");
            private final ContextMenu contextMenu = new ContextMenu(copyCellItem, copyRowItem);

            {
                contextMenu.getStyleClass().add("table-context-menu");
                copyCellItem.getStyleClass().add("table-context-menu-item");
                copyRowItem.getStyleClass().add("table-context-menu-item");

                copyCellItem.setOnAction(e -> {
                    List<CellValue> row = getTableRow() != null ? getTableRow().getItem() : null;
                    if (row == null || columnIndex >= row.size()) {
                        return;
                    }
                    ClipboardUtil.copy(toCopyText(row.get(columnIndex)));
                });

                copyRowItem.setOnAction(e -> {
                    List<CellValue> row = getTableRow() != null ? getTableRow().getItem() : null;
                    if (row == null) {
                        return;
                    }
                    ClipboardUtil.copy(formatRowForCopy(row));
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText(item);
                    setContextMenu(contextMenu);
                }
            }
        };
    }

    /** Null → "NULL", blank → "" (empty string), otherwise the cell text. */
    static String toCopyText(CellValue cell) {
        if (cell == null || cell.isNull()) {
            return "NULL";
        }
        if (cell.getText().isEmpty()) {
            return "__EMPTY__";
        }
        return cell.getText();
    }

    static String formatRowForCopy(List<CellValue> row) {
        return row.stream()
                .map(TableComponent::toCopyText)
                .collect(Collectors.joining(","));
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
