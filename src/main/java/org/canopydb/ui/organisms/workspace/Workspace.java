package org.canopydb.ui.organisms.workspace;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.config.Profiler;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.QuerySession;
import org.canopydb.models.TableSession;

import java.util.HashMap;

/**
 * Manages workspace tabs: table browse sessions and SQL editor sessions.
 */
public class Workspace {
    private final VBox workspace = new VBox();
    private final TabPane tabs = new TabPane();

    private final HashMap<String, TableTab> tableTabs = new HashMap<>();
    private final HashMap<String, QueryTab> queryTabs = new HashMap<>();

    private final TableViewEventController tableViewEventController;

    public Workspace() {
        tableViewEventController = new TableViewEventController(this::updateSession);
        workspace.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public VBox getWorkspace() {
        return workspace;
    }

    public void addNewSession(TableSession tableSession) {
        TableTab tableTab = new TableTab(tableSession, tableViewEventController);
        tableTabs.put(tableSession.getTablePath(), tableTab);

        Tab tab = tableTab.getTab();
        tab.setOnClosed(event -> {
            tableViewEventController.cancelPending(tableSession.getTablePath());
            tableTabs.remove(tableSession.getTablePath());
            tableTab.dispose();
            Profiler.logMemory();
        });
        tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        Profiler.logMemory();
    }

    public void openQueryTab() {
        QuerySession session = new QuerySession();
        QueryTab queryTab = new QueryTab(session);
        queryTabs.put(session.getId(), queryTab);

        Tab tab = queryTab.getTab();
        tab.setOnClosed(event -> {
            queryTabs.remove(session.getId());
            queryTab.cancelPending();
            queryTab.dispose();
            Profiler.logMemory();
        });
        tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        Profiler.logMemory();
    }

    public void updateSession(TableSession tableSession) {
        TableTab tab = tableTabs.get(tableSession.getTablePath());
        if (tab != null) {
            tab.updateSession(tableSession);
        }
    }

    public boolean isSessionActive(String tableKey) {
        return tableTabs.containsKey(tableKey);
    }

    public boolean selectActiveSession(String tableKey) {
        if (!isSessionActive(tableKey)) {
            return false;
        }
        TableTab tab = tableTabs.get(tableKey);
        tabs.getSelectionModel().select(tab.getTab());
        return true;
    }
}
