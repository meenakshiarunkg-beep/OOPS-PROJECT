package login;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;
import voter.VoterDashboard;
import admin.AdminLogin;

/**
 * LoginFrame
 * -----------
 * The first screen a voter sees.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Online Voting System - Login");
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Voter Login", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register as New Voter");
        JButton adminBtn = new JButton("Admin Login");

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Username label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Username:"), gbc);

        // Username field
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        // Password label
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        // Password field
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);

        // Register button
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(registerBtn, gbc);

        // Admin button
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(adminBtn, gbc);

        add(panel);

        // Button actions
        loginBtn.addActionListener(e -> attemptLogin());

        registerBtn.addActionListener(e -> {
            new RegistrationFrame().setVisible(true);
        });

        adminBtn.addActionListener(e -> {
            new AdminLogin().setVisible(true);
            dispose();
        });
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both username and password."
            );
            return;
        }

        String sql =
                "SELECT voter_id, name, approved " +
                "FROM voters WHERE username = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    boolean approved = rs.getBoolean("approved");

                    if (!approved) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Your registration is pending admin approval."
                        );
                        return;
                    }

                    int voterId = rs.getInt("voter_id");
                    String name = rs.getString("name");

                    new VoterDashboard(voterId, name).setVisible(true);
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username or password."
                    );
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error: " + ex.getMessage()
            );
        }
    }
}