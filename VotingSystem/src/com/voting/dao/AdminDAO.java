package com.voting.dao;

import com.voting.db.DBConnection;
import com.voting.model.AdminUser;
import com.voting.util.PasswordUtil;

import java.sql.*;

public class AdminDAO {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "Admin@123";

    /**
     * Called once at application startup. If the admin table is empty
     * (fresh database), it creates the default admin account so there is
     * always a way in.
     */
    public void bootstrapDefaultAdminIfNeeded() {
        String countSql = "SELECT COUNT(*) FROM admin";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hash(DEFAULT_PASSWORD, salt);
                String insertSql = "INSERT INTO admin (username, password_hash, salt) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, DEFAULT_USERNAME);
                    ps.setString(2, hash);
                    ps.setString(3, salt);
                    ps.executeUpdate();
                    System.out.println("Default admin created -> username: admin | password: Admin@123");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns the AdminUser if the credentials are correct, otherwise null. */
   public AdminUser authenticate(String username, String plainPassword) {

    String sql = "SELECT admin_id, username, password_hash, salt " +
                 "FROM admin WHERE username = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, username);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                String hash = rs.getString("password_hash");
                String salt = rs.getString("salt");

                if (PasswordUtil.verify(plainPassword, salt, hash)) {
                    return new AdminUser(
                        rs.getInt("admin_id"),
                        rs.getString("username")
                    );
                }
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}

    public boolean changePassword(int adminId, String newPlainPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPlainPassword, salt);
        String sql = "UPDATE admin SET password_hash = ?, salt = ? WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
