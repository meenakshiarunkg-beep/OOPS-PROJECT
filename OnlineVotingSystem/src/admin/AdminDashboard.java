package admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;
import voting.ResultsFrame;
import login.LoginFrame;

/**
 * AdminDashboard
 * ---------------
 * The control panel for the admin: add candidates, view candidates,
 * approve pending voters, and jump to the results screen.
 */
public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1, 5, 5));

        JButton addCandidateBtn = new JButton("Add Candidate");
        JButton viewCandidatesBtn = new JButton("View Candidates");
        JButton approveVotersBtn = new JButton("View / Approve Voters");
        JButton viewResultsBtn = new JButton("View Results");
        JButton logoutBtn = new JButton("Logout");

        addCandidateBtn.addActionListener(e -> addCandidate());
        viewCandidatesBtn.addActionListener(e -> viewCandidates());
        approveVotersBtn.addActionListener(e -> new VoterApprovalDialog(this).setVisible(true));
        viewResultsBtn.addActionListener(e -> new ResultsFrame().setVisible(true));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        add(addCandidateBtn);
        add(viewCandidatesBtn);
        add(approveVotersBtn);
        add(viewResultsBtn);
        add(logoutBtn);
    }

    private void addCandidate() {
        JTextField nameField = new JTextField();
        JTextField partyField = new JTextField();
        Object[] fields = {
                "Candidate name:", nameField,
                "Party:", partyField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Candidate",
                JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String party = partyField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Candidate name is required.");
            return;
        }

        String sql = "INSERT INTO candidates (name, party) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, party);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Candidate added.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void viewCandidates() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT name, party FROM candidates";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append(rs.getString("name")).append(" (").append(rs.getString("party")).append(")\n");
            }
            if (sb.length() == 0) sb.append("No candidates added yet.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Candidates", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Small inner dialog listing voters who are not yet approved, with an
     * Approve button per row. Kept as an inner class since it's only ever
     * opened from here.
     */
    private static class VoterApprovalDialog extends JDialog {

        VoterApprovalDialog(JFrame parent) {
            super(parent, "Pending Voter Approvals", true);
            setSize(400, 300);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            String[] columns = {"Voter ID", "Name", "Username"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            loadPendingVoters(model);

            JTable table = new JTable(model);
            JButton approveBtn = new JButton("Approve Selected");

            approveBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a voter first.");
                    return;
                }
                int voterId = (int) model.getValueAt(row, 0);
                approveVoter(voterId);
                model.removeRow(row);
            });

            add(new JScrollPane(table), BorderLayout.CENTER);
            add(approveBtn, BorderLayout.SOUTH);
        }

        private void loadPendingVoters(DefaultTableModel model) {
            String sql = "SELECT voter_id, name, username FROM voters WHERE approved = FALSE";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("voter_id"),
                            rs.getString("name"),
                            rs.getString("username")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }

        private void approveVoter(int voterId) {
            String sql = "UPDATE voters SET approved = TRUE WHERE voter_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, voterId);
                ps.executeUpdate();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }
}
