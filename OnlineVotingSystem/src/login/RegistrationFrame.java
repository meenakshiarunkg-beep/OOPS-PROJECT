package login;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;

/**
 * RegistrationFrame
 * ------------------
 * New voters sign up here. voter_id is NOT typed by the user - the
 * database auto-generates it (see AUTO_INCREMENT in the SQL script).
 * New voters start as approved = FALSE and must wait for an admin to
 * approve them before they can log in.
 */
public class RegistrationFrame extends JFrame {

    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public RegistrationFrame() {
        setTitle("Voter Registration");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1, 5, 5));

        nameField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> registerVoter());

        add(new JLabel("Full Name:"));
        add(nameField);
        add(new JLabel("Choose a Username:"));
        add(usernameField);
        add(new JLabel("Choose a Password:"));
        add(passwordField);
        add(registerBtn);
    }

    private void registerVoter() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        if (usernameExists(username)) {
            JOptionPane.showMessageDialog(this, "That username is already taken.");
            return;
        }

        String sql = "INSERT INTO voters (name, username, password, approved, has_voted) " +
                     "VALUES (?, ?, ?, FALSE, FALSE)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Registered! An admin must approve your account before you can log in.");
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private boolean usernameExists(String username) {
        String sql = "SELECT voter_id FROM voters WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            return true; // fail safe: block registration rather than risk a duplicate
        }
    }
}
