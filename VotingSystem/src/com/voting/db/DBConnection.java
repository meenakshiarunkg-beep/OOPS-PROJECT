package com.voting.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a fresh JDBC connection on every call. Each DAO method is expected
 * to use this inside a try-with-resources block so the connection is
 * always closed, even for multi-statement transactions.
 *
 * ==>  CHANGE the USER / PASSWORD constants below to match your own
 *      local MySQL installation before running the app.
 */
public final class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/voting_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";      // <-- your MySQL username
    private static final String PASSWORD = "hana@2006";  // <-- your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found on the classpath. " +
                    "Make sure mysql-connector-j-*.jar is added (see /lib and README).");
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
