package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection
 * -------------
 * ONE place that knows how to connect to the database. Every other class
 * (login, registration, admin, voting, results) calls
 * DBConnection.getConnection() instead of writing its own connection code.
 * This is "abstraction" - other classes use the database without knowing
 * the connection details.
 *
 * EDIT THESE THREE VALUES to match your own MySQL setup:
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/online_voting";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // XAMPP's default MySQL root password is usually blank

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Quick manual test. Run this file directly (not the whole app) to
     * confirm the database connection works before building anything else.
     */
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            System.out.println("Database Connected Successfully");
        } catch (SQLException ex) {
            System.out.println("Connection failed: " + ex.getMessage());
        }
    }
}
