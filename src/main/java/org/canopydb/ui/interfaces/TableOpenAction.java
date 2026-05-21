package org.canopydb.ui.interfaces;

import org.canopydb.entities.TableData;

@FunctionalInterface
public interface TableOpenAction {
    void render(TableData tableData);
}

