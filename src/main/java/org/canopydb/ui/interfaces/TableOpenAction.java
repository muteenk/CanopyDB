package org.canopydb.ui.interfaces;

import org.canopydb.models.TableData;

@FunctionalInterface
public interface TableOpenAction {
    void render(TableData tableData);
}

