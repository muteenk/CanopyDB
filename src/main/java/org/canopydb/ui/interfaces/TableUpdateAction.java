package org.canopydb.ui.interfaces;

import org.canopydb.models.TableSession;

@FunctionalInterface
public interface TableUpdateAction {
    void render(TableSession tableSession);
}
