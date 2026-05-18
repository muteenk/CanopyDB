package org.canopydb.db;

import org.canopydb.config.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetadataDAO {
    public List<String> getAllDatabases() throws SQLException {
        List<String> databases = new ArrayList<>();
        String sql = "SHOW DATABASES";

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // The column name returned by MySQL is "Database"
                databases.add(rs.getString("Database"));
            }
        }
        return databases;
    }
}
