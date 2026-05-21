package org.canopydb.repository;

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
             PreparedStatement pStmt = conn.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery()) {

            while (rs.next()) {
                // The column name returned by MySQL is "Database"
                databases.add(rs.getString("Database"));
            }
        }
        return databases;
    }

    public List<String> getAllTablesByDatabase(String database) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {

            // Bind the database name safely to prevent SQL Injection
            pStmt.setString(1, database);

            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    // Column 1 contains the TABLE_NAME string
                    String tableName = rs.getString(1);
                    tables.add(tableName);
                }
            }
        }
        return tables;
    }
}
