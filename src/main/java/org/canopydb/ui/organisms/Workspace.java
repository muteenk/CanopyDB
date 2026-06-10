package org.canopydb.ui.organisms;

import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.canopydb.controllers.TableViewEventController;
import org.canopydb.models.TableSession;
import org.canopydb.ui.interfaces.PushNotification;

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

    private final HashMap<String, TableSession> activeSessions = new HashMap<>();
    private final HashMap<String, TableTab> activeTabs = new HashMap<>();

    private final TableViewEventController tableViewEventController;

    public Workspace(PushNotification pushNotification){
        tableViewEventController = new TableViewEventController(
                this::updateTable,
                pushNotification
        );
        workspace.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    public VBox getWorkspace() {
        return workspace;
    }

    public void addNewSession(TableSession tableSession) {
        String tablePath = tableSession.getTablePath();
        TableTab tabSession = new TableTab(tableSession, tableViewEventController);
        activeSessions.put(tableSession.getTablePath(), tableSession);
        activeTabs.put(tableSession.getTablePath(), tabSession);

        Tab tab = tabSession.getTab();
        tab.setOnClosed(event -> {
            activeSessions.remove(tablePath);
            activeTabs.remove(tablePath);
        });
        tabs.getTabs().add(tab);
    }

    public void updateTable(TableSession tableSession) {
        activeTabs.get(tableSession.getTablePath()).updateSession(tableSession);
        activeSessions.put(tableSession.getTablePath(), tableSession);
    }

    public boolean isSessionActive(String table) {
        return activeSessions.containsKey(table);
    }
}
