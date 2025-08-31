package blockchain_ticketing_system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Connect_Db {
    private static final String URL = "jdbc:mysql://localhost:3306/blockchain_ticketing_system";
    private static final String USER = "cbs";
    private static final String PASSWORD = "M0nkrus@2025";
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    // To create a new DB connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
