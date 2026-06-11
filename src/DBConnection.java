import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv().getOrDefault(
        "DB_URL",
        "jdbc:mysql://mysql-20e67389-dhanushoff28-53cc.d.aivencloud.com:22386/defaultdb?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );

    private static final String USER = System.getenv().getOrDefault("DB_USER", "avnadmin");

    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found. Check JAR file.", e);
        }
    }
}