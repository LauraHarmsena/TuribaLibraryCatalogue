import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection connection = null;
    public static Connection connect() {
        if (connection == null) {
            try {
                String url = "jdbc:sqlite:library.db";
                connection = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println("Connection error: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.out.println("Disconnection error: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connect();
    }
}
