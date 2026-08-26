package org.canopydb.repository;

import org.canopydb.config.AppLogger;
import org.canopydb.config.DatabasePool;
import org.canopydb.models.CellValue;
import org.canopydb.models.ColumnMeta;
import org.canopydb.models.QueryResult;
import org.canopydb.models.TableData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Executes ad-hoc SQL against the active pool.
 * Supports both result-set queries and update/DDL statements.
 */
public class QueryActionDAO {
    private static final Logger LOGGER = AppLogger.getLogger(QueryActionDAO.class);

    public QueryResult execute(String sql, QueryHandle handle) throws SQLException {
        LOGGER.fine(() -> "Executing ad-hoc SQL: " + sql);
        long started = System.nanoTime();

        try (Connection conn = DatabasePool.getConnection();
             Statement statement = conn.createStatement()) {
            handle.register(statement);
            try {
                boolean hasResultSet = statement.execute(sql);
                long durationMs = (System.nanoTime() - started) / 1_000_000L;

                if (hasResultSet) {
                    try (ResultSet rs = statement.getResultSet()) {
                        TableData tableData = readResultSet(rs, handle);
                        return QueryResult.ofResultSet(tableData, durationMs);
                    }
                }

                return QueryResult.ofUpdate(statement.getUpdateCount(), durationMs);
            } finally {
                handle.clear();
            }
        }
    }

    private static TableData readResultSet(ResultSet rs, QueryHandle handle) throws SQLException {
        TableData tableData = new TableData();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            tableData.appendColumn(new ColumnMeta(
                    metaData.getColumnLabel(i),
                    metaData.getColumnType(i),
                    metaData.getColumnTypeName(i)
            ));
        }

        while (rs.next()) {
            handle.checkCancelled();
            List<CellValue> row = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                int jdbcType = tableData.getColumns().get(i - 1).getJdbcType();
                row.add(ResultSetValueSerializer.read(rs, i, jdbcType));
            }
            tableData.appendRow(row);
        }
        return tableData;
    }
}
