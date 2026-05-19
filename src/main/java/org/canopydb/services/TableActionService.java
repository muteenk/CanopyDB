package org.canopydb.services;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.canopydb.repository.TableActionDAO;
import org.canopydb.ui.organisms.MiddlePane;

import java.util.List;

public class TableActionService {
    TableActionDAO tableActionDAO = new TableActionDAO();

    public void loadTableDataAsync(TreeItem<String> node, MiddlePane middle){
        Task<List<List<String>>> fetchTable = new Task<>() {
            @Override
            protected List<List<String>> call() throws Exception {
                return tableActionDAO.getTableData(
                        node.getValue(), node.getParent().getValue()
                );
            }
        };

        fetchTable.setOnSucceeded(event -> {
            List<List<String>> table = fetchTable.getValue();
            TableView<List<String>> tableView = new TableView<>();
            tableView.getColumns().clear();
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
            middle.addTable(tableView);
        });

        fetchTable.setOnFailed(event -> {
            Throwable error = fetchTable.getException();
            System.err.println("FAILLLLLLLLLLLLLL: " + error.getMessage());
        });

        Thread task = new Thread(fetchTable);
        task.start();
    }
}
