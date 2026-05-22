package org.canopydb.ui.interfaces;

import org.canopydb.models.TableData;
import org.canopydb.models.TableSession;

@FunctionalInterface
public interface TableOpenAction {
    void render(TableSession tableSession);
}

