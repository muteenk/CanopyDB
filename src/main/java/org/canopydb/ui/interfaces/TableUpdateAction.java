package org.canopydb.ui.interfaces;

import javafx.scene.control.TableView;
import org.canopydb.models.TableData;
import org.canopydb.models.TableSession;

import java.util.List;

@FunctionalInterface
public interface TableUpdateAction {
    void render(TableSession tableSession, TableView<List<String>> tableView);
}
