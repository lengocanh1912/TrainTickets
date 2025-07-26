package t3h.edu.vn.traintickets.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    public static String username = "root";
    public static String password = "anh123";
    public static String connectionURL = "jdbc:mysql://localhost:3306/Trainticketsdb";
    static Connection conn = null;
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (conn == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(connectionURL, username, password);
        }
        return conn;
    }
}
