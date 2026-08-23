package org.canopydb.ui.organisms.workspace;

import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableSession;
import org.canopydb.config.Profiler;

import java.util.HashMap;

/*                  Workspace Responsibility
* The single responsibility for Workspace is to manage sessions.
* Neither table internals nor tab internals, just sessions.
*
* A session here refers to an active tab along with its table session component.
* */

public class Workspace {
    private final VBox workspace = new VBox();
    private final TabPane tabs = new TabPane();

    private final HashMap<String, TableTab> activeTabs = new HashMap<>();

    private final TableViewEventController tableViewEventController;

    public Workspace(){
        tableViewEventController = new TableViewEventController(this::updateSession);
        workspace.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public VBox getWorkspace() {
        return workspace;
    }

    public void addNewSession(TableSession tableSession) {
        TableTab tableTab = new TableTab(tableSession, tableViewEventController);
        activeTabs.put(tableSession.getTablePath(), tableTab);

        Tab tab = tableTab.getTab();
        tab.setOnClosed(event -> {
            tableViewEventController.cancelPending(tableSession.getTablePath());
            activeTabs.remove(tableSession.getTablePath());
            tableTab.dispose();
            Profiler.logMemory();
        });
        tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        Profiler.logMemory();
    }

    public void updateSession(TableSession tableSession) {
        activeTabs.get(tableSession.getTablePath()).updateSession(tableSession);
    }

    public boolean isSessionActive(String tableKey) {return activeTabs.containsKey(tableKey);}

    public boolean selectActiveSession(String tableKey) {
        if (!isSessionActive(tableKey)) return false;
        TableTab tab = this.activeTabs.get(tableKey);
        this.tabs.getSelectionModel().select(tab.getTab());
        return true;
    }
}
