package org.canopydb.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.canopydb.models.ConnectionMeta;
import org.canopydb.utils.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

public final class DatabasePool {

    private static final Object LOCK = new Object();
    private static final AtomicLong poolEpoch = new AtomicLong();

    private static HikariDataSource dataSource;

    private DatabasePool() {}

    /**
     * Replaces the active pool and verifies a connection can be obtained.
     *
     * @return pool epoch for this connect — use with {@link #disconnectIfEpoch(long)}
     *         if the caller abandons the attempt after it succeeds
     */
    public static long connect(ConnectionMeta connection) throws SQLException {
        synchronized (LOCK) {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(buildJdbcUrl(connection));
            config.setUsername(connection.getUsername());
            config.setPassword(connection.getPassword());

            config.setMaximumPoolSize(10);
            config.setIdleTimeout(30_000);
            config.setConnectionTimeout(Constants.HIKARI_CONNECTION_TIMEOUT_MS);
            config.setValidationTimeout(Constants.HIKARI_VALIDATION_TIMEOUT_MS);

            dataSource = new HikariDataSource(config);
            dataSource.getConnection().close();
            return poolEpoch.incrementAndGet();
        }
    }

    /**
     * Verifies credentials without replacing the active pool.
     */
    public static void testConnection(ConnectionMeta connection) throws SQLException {
        try (Connection ignored = DriverManager.getConnection(
                buildTestJdbcUrl(connection),
                connection.getUsername(),
                connection.getPassword()
        )) {
            // Connection succeeded if we reach here.
        }
    }

    public static Connection getConnection() throws SQLException {
        synchronized (LOCK) {
            if (dataSource == null) {
                throw new IllegalStateException("No active database connection.");
            }
            return dataSource.getConnection();
        }
    }

    public static void disconnect() {
        synchronized (LOCK) {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    /**
     * Closes the pool only if it still belongs to {@code epoch}
     * (e.g. an abandoned connect that finished after the user switched forms).
     */
    public static void disconnectIfEpoch(long epoch) {
        synchronized (LOCK) {
            if (poolEpoch.get() == epoch && dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    private static String buildJdbcUrl(ConnectionMeta connection) {
        return buildUrl(connection, Constants.JDBC_QUERY_SOCKET_TIMEOUT_MS);
    }

    private static String buildTestJdbcUrl(ConnectionMeta connection) {
        return buildUrl(connection, Constants.JDBC_TEST_SOCKET_TIMEOUT_MS);
    }

    private static String buildUrl(ConnectionMeta connection, int socketTimeoutMs) {
        return "jdbc:mysql://"
                + connection.getHost()
                + ":"
                + connection.getPort()
                + "/"
                + nullToEmpty(connection.getDatabase())
                + "?connectTimeout=" + Constants.JDBC_CONNECT_TIMEOUT_MS
                + "&socketTimeout=" + socketTimeoutMs;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
