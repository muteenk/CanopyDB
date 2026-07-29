package org.canopydb.repository;

import org.canopydb.config.AppLogger;
import org.canopydb.config.DatabasePool;
import org.canopydb.models.TableData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TableActionDAO {
    private static final Logger LOGGER = AppLogger.getLogger(TableActionDAO.class);

    public TableData getTableData(String sql) throws SQLException {
        LOGGER.fine(() -> "Executing SQL: " + sql);
        TableData tableDataObject = new TableData();

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String colName = metaData.getColumnName(i);
                tableDataObject.appendHeader(colName);
            }

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    row.add(value != null ? value.toString() : "NULL");
                }
                tableDataObject.appendRow(row);
            }
            return tableDataObject;
        }
    }

    public int getTableDataCount(String sql) throws SQLException {
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery()) {
            int tableDataCount = 0;
            if (rs.next()) tableDataCount = rs.getInt("row_count");
            return tableDataCount;
        }
    }
}
