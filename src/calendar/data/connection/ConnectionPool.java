package calendar.data.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private static final String JDBC_URL = "jdbc:postgresql://127.0.0.1:5432/lee";
    private static final String USERNAME = "lee";
    private static final String PASSOWRD = "lee";
    private static final int MAX_POOL_SIZE = 10;

    private final List<Connection> connectionPool;

    public ConnectionPool() {
        connectionPool = new ArrayList<>(MAX_POOL_SIZE);
        initializePool();
    }

    private void initializePool() {
        try {
            for (int i=0; i<MAX_POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSOWRD);
                conn.setAutoCommit(false);
                connectionPool.add(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        // 'synchronized' for thread safety
        while (connectionPool.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return connectionPool.remove(connectionPool.size() - 1);
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            connectionPool.add(conn);
            notify();
        }
    }

    public synchronized void closeAllConnections() throws SQLException {
        for (Connection conn: connectionPool) {
            conn.close();
        }
        connectionPool.clear();
    }

}
