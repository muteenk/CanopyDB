package org.canopydb.ui.organisms;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.entities.TableData;
import org.canopydb.ui.molecules.TableComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MiddlePane {
    private final VBox middleArea;
    private final TabPane tabs;
    private final HashSet<String> activeTables;

    public MiddlePane(){
        activeTables = new HashSet<>();
        tabs = new TabPane();
        middleArea = new VBox(tabs);
    }

    public VBox getMiddle() {
        return middleArea;
    }

    public void addTable(TableData tableData) {
        TableView<List<String>> tableView = new TableComponent(tableData).getTable();
        String tablePath = tableData.getTablePath();
        Tab newTab = new Tab(tablePath);
        newTab.setContent(tableView);
        newTab.setOnClosed(event -> {
            activeTables.remove(tablePath);
        });
        tabs.getTabs().add(newTab);
        activeTables.add(tablePath);

        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public boolean isTableActive(String table) {
        return activeTables.contains(table);
    }


}
