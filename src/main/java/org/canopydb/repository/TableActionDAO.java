package org.canopydb.repository;

import org.canopydb.config.AppLogger;
import org.canopydb.config.DatabasePool;
import org.canopydb.models.CellValue;
import org.canopydb.models.ColumnMeta;
import org.canopydb.models.TableData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TableActionDAO {
    private static final Logger LOGGER = AppLogger.getLogger(TableActionDAO.class);

    public TableData getTableData(String sql, QueryHandle handle) throws SQLException {
        LOGGER.fine(() -> "Executing SQL: " + sql);
        TableData tableDataObject = new TableData();

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            handle.register(pStmt);
            try (ResultSet rs = pStmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    tableDataObject.appendColumn(new ColumnMeta(
                            metaData.getColumnLabel(i),
                            metaData.getColumnType(i),
                            metaData.getColumnTypeName(i)
                    ));
                }

                while (rs.next()) {
                    handle.checkCancelled();
                    List<CellValue> row = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        int jdbcType = tableDataObject.getColumns().get(i - 1).getJdbcType();
                        row.add(ResultSetValueSerializer.read(rs, i, jdbcType));
                    }
                    tableDataObject.appendRow(row);
                }
                return tableDataObject;
            } finally {
                handle.clear();
            }
        }
    }

    public int getTableDataCount(String sql, QueryHandle handle) throws SQLException {
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            handle.register(pStmt);
            try (ResultSet rs = pStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("row_count");
                }
                return 0;
            } finally {
                handle.clear();
            }
        }
    }
}
