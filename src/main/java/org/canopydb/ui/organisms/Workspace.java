package org.canopydb.ui.organisms;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.entities.TableData;
import org.canopydb.ui.molecules.TableComponent;

import java.util.HashSet;
import java.util.List;

public class Workspace {
    private final VBox workspace;
    private final TabPane tabs;
    private final HashSet<String> activeTables;

    public Workspace(){
        activeTables = new HashSet<>();
        tabs = new TabPane();
        workspace = new VBox(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public VBox getWorkspace() {
        return workspace;
    }

    private Tab buildTab(String tablePath, TableView<List<String>> tableView) {
        Tab tab = new Tab(tablePath);
        tab.setContent(tableView);
        tab.setOnClosed(event -> {
            activeTables.remove(tablePath);
        });
        return tab;
    }

    public void addTable(TableData tableData) {
        TableView<List<String>> tableView = new TableComponent(tableData).getTable();
//        tableView.setOnSort(event -> {
//            event.consume();
//            TableColumn<List<String>, ?> selectedColumn = tableView.getSortOrder().getFirst();
//            TableColumn.SortType sortType = selectedColumn.getSortType();
//
//        });

        String tablePath = tableData.getTablePath();
        tabs.getTabs().add(buildTab(tablePath, tableView));
        activeTables.add(tablePath);
    }

    public boolean isTableOpen(String table) {
        return activeTables.contains(table);
    }
}
