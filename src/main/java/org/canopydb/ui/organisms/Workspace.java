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

    private final HashMap<String, TableTab> activeTabs = new HashMap<>();

    private final TableViewEventController tableViewEventController;

    public Workspace(PushNotification pushNotification){
        tableViewEventController = new TableViewEventController(
                this::updateSession,
                pushNotification
        );
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
            activeTabs.remove(tableSession.getTablePath());
        });
        tabs.getTabs().add(tab);
    }

    public void updateSession(TableSession tableSession) {
        activeTabs.get(tableSession.getTablePath()).updateSession(tableSession);
    }

    public boolean isSessionActive(String table) {return activeTabs.containsKey(table);}
}
