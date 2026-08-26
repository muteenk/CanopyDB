package org.canopydb.models;

/**
 * Outcome of an ad-hoc SQL execution: either a result grid or an update count.
 */
public final class QueryResult {

    private final TableData tableData;
    private final int updateCount;
    private final boolean hasResultSet;
    private final long durationMs;

    private QueryResult(TableData tableData, int updateCount, boolean hasResultSet, long durationMs) {
        this.tableData = tableData;
        this.updateCount = updateCount;
        this.hasResultSet = hasResultSet;
        this.durationMs = durationMs;
    }

    public static QueryResult ofResultSet(TableData tableData, long durationMs) {
        return new QueryResult(tableData, -1, true, durationMs);
    }

    public static QueryResult ofUpdate(int updateCount, long durationMs) {
        return new QueryResult(null, updateCount, false, durationMs);
    }

    public boolean hasResultSet() {
        return hasResultSet;
    }

    public TableData getTableData() {
        return tableData;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String statusMessage() {
        if (hasResultSet) {
            int rows = tableData == null ? 0 : tableData.getRows().size();
            return rows + " row" + (rows == 1 ? "" : "s") + " · " + durationMs + " ms";
        }
        if (updateCount < 0) {
            return "OK · " + durationMs + " ms";
        }
        return updateCount + " row" + (updateCount == 1 ? "" : "s") + " affected · " + durationMs + " ms";
    }
}
