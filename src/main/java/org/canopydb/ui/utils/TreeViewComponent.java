package org.canopydb.ui.utils;

import javafx.scene.control.TreeItem;

public class TreeViewComponent {

    public static int getNodeDepth(TreeItem<String> item){
        // Level 0 = Root Server ("Databases" Root)
        // Level 1 = Database Node ("Database")
        // Level 2 = Table Node

        int depth = 0;
        TreeItem<String> current = item;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }

        return depth;
    }

    public static boolean isTableNode(TreeItem<String> item) {
        return TreeViewComponent.getNodeDepth(item) == 2;
    }
}
