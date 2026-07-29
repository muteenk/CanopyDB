package org.canopydb.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.canopydb.models.ConnectionMeta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabasePool {

    private static HikariDataSource dataSource;

    private DatabasePool() {}

    public static void connect(
            ConnectionMeta connection
    ) throws SQLException {
        if (dataSource != null) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(buildJdbcUrl(connection));
        config.setUsername(connection.getUsername());
        config.setPassword(connection.getPassword());

        config.setMaximumPoolSize(10);
        config.setIdleTimeout(30000);

        dataSource = new HikariDataSource(config);
        dataSource.getConnection().close();
    }

    /**
     * Verifies credentials without replacing the active pool.
     */
    public static void testConnection(ConnectionMeta connection) throws SQLException {
        try (Connection ignored = DriverManager.getConnection(
                buildJdbcUrl(connection),
                connection.getUsername(),
                connection.getPassword()
        )) {
            // Connection succeeded if we reach here.
        }
    }

    public static Connection getConnection()
            throws SQLException {

        if (dataSource == null) {
            throw new IllegalStateException(
                    "No active database connection."
            );
        }

        return dataSource.getConnection();
    }

    public static void disconnect() {

        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static String buildJdbcUrl(ConnectionMeta connection) {
        return "jdbc:mysql://"
                + connection.getHost()
                + ":"
                + connection.getPort()
                + "/";
    }

}
