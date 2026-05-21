package org.canopydb.ui.interfaces;

import javafx.scene.control.TableView;
import org.canopydb.models.TableData;

import java.util.List;

@FunctionalInterface
public interface TableUpdateAction {
    void render(TableData tableData, TableView<List<String>> tableView);
}
