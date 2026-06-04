package org.canopydb.repository;

import org.canopydb.config.DatabasePool;
import org.canopydb.models.TableData;
import org.canopydb.queries.TableQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableActionDAO {
    public TableData getTableData(String sql) throws SQLException {
        System.out.println(sql);    // TODO: REMOVE
        TableData tableDataObject = new TableData();

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String col_name = metaData.getColumnName(i);
                tableDataObject.appendHeader(col_name);
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
