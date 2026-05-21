package org.canopydb.repository;

import org.canopydb.config.DatabasePool;
import org.canopydb.entities.TableData;
import org.canopydb.queries.TableQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableActionDAO {
    public TableData getTableData(String table, String database, TableQuery query) throws SQLException {
        String sql = query.getQuery();
        System.out.println(sql);    // TODO: REMOVE
        TableData tableDataObject = new TableData(table, database, query);

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
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
}
