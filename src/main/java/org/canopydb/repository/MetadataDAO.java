package org.canopydb.repository;

import org.canopydb.config.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetadataDAO {

    public List<String> getAllDatabases(QueryHandle handle) throws SQLException {
        List<String> databases = new ArrayList<>();
        String sql = "SHOW DATABASES";

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            handle.register(pStmt);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    handle.checkCancelled();
                    databases.add(rs.getString("Database"));
                }
            } finally {
                handle.clear();
            }
        }
        return databases;
    }

    public List<String> getAllTablesByDatabase(String database, QueryHandle handle) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setString(1, database);
            handle.register(pStmt);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    handle.checkCancelled();
                    tables.add(rs.getString(1));
                }
            } finally {
                handle.clear();
            }
        }
        return tables;
    }
}
