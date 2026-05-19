package org.canopydb.ui.interfaces;

import org.canopydb.entities.TableData;

@FunctionalInterface
public interface TableDataAppendAction {
    void render(TableData tableData);
}
