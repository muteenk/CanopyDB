package org.canopydb.ui.interfaces;

import java.util.List;

@FunctionalInterface
public interface TableDataAppendAction {
    void render(List<List<String>> tableData);
}
