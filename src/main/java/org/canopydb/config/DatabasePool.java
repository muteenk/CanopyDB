package org.canopydb.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.canopydb.models.SavedConnection;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabasePool {

    private static HikariDataSource dataSource;

    private DatabasePool() {}

    public static synchronized void connect(
            SavedConnection connection
    ) {

        if (dataSource != null) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(
                "jdbc:mysql://" +
                        connection.getHost() +
                        ":" +
                        connection.getPort() +
                        "/"
        );

        config.setUsername(connection.getUsername());
        config.setPassword(connection.getPassword());

        config.setMaximumPoolSize(10);
        config.setIdleTimeout(30000);

        dataSource = new HikariDataSource(config);
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

}
