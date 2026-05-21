package org.canopydb.ui.interfaces;

import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import org.canopydb.entities.TableData;

import java.util.List;

@FunctionalInterface
public interface TableUpdateAction {
    void render(TableData tableData, TableView<List<String>> tableView);
}
