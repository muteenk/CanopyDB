package org.canopydb.repository;

import org.canopydb.config.DatabasePool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableActionDAO {
    public List<List<String>> getTableData(String table, String database) throws SQLException {
        String sql = "SELECT * FROM `" + database + "`.`"+ table +"` ORDER BY `id` LIMIT 300 OFFSET 0;";
        List<List<String>> dataRows = new ArrayList<>();

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> cols = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String col_name = metaData.getColumnName(i);
                cols.add(col_name);
            }
            dataRows.add(cols);

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    row.add(value != null ? value.toString() : "NULL");
                }

                dataRows.add(row);
            }
            return dataRows;
        }
    }
}
