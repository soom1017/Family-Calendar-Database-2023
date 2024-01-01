package calendar.data.connection;

import java.sql.Connection;
import java.sql.SQLException;


public class DatabaseManager {
    private static ConnectionPool connectionPool;
    
    static {
        connectionPool = new ConnectionPool();
    }

    public static Connection getConnection() {
        return connectionPool.getConnection();
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void releaseConnection(Connection connection) {
        connectionPool.releaseConnection(connection);
    }

    public static void closeAllConnections() {
        try {
            connectionPool.closeAllConnections();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
