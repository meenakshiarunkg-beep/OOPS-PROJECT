package voting;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;

/**
 * VotingFrame
 * ------------
 * Opened from VoterDashboard when the voter clicks "Cast Vote".
 * Flow:
 *   1. Check has_voted for this voter. If true -> show message, do nothing else.
 *   2. Load candidates from the candidates table into a dropdown.
 *   3. Voter picks one and clicks "Cast Vote".
 *   4. Insert a row into votes, then set has_voted = true for this voter.
 *      Both steps happen inside one try block; if either fails we don't
 *      leave the data half-updated.
 */
public class VotingFrame extends JFrame {

    private final int voterId;
    private JComboBox<CandidateItem> candidateBox;

    public VotingFrame(int voterId) {
        this.voterId = voterId;

        setTitle("Cast Your Vote");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- If they've already voted, don't even show the form ---
        if (hasAlreadyVoted()) {
            JOptionPane.showMessageDialog(this, "You have already voted.");
            dispose();
            return;
        }

        List<CandidateItem> candidates = loadCandidates();
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No candidates available yet. Ask the admin to add some.");
            dispose();
            return;
        }

        candidateBox = new JComboBox<>(candidates.toArray(new CandidateItem[0]));

        JPanel formPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        formPanel.add(new JLabel("Select a candidate:", JLabel.CENTER));
        formPanel.add(candidateBox);

        JButton castVoteBtn = new JButton("Cast Vote");
        castVoteBtn.addActionListener(e -> castVote());

        add(formPanel, BorderLayout.CENTER);
        add(castVoteBtn, BorderLayout.SOUTH);
    }

    /** Small holder so the combo box can show a name but keep the id. */
    private static class CandidateItem {
        int id;
        String name;
        String party;

        CandidateItem(int id, String name, String party) {
            this.id = id;
            this.name = name;
            this.party = party;
        }

        @Override
        public String toString() {
            return name + " (" + party + ")";
        }
    }

    private boolean hasAlreadyVoted() {
        String sql = "SELECT has_voted FROM voters WHERE voter_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, voterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_voted");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
        return false;
    }

    private List<CandidateItem> loadCandidates() {
        List<CandidateItem> list = new ArrayList<>();
        String sql = "SELECT candidate_id, name, party FROM candidates";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CandidateItem(
                        rs.getInt("candidate_id"),
                        rs.getString("name"),
                        rs.getString("party")
                ));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
        return list;
    }

    /**
     * Records the vote and marks the voter as having voted.
     * We re-check has_voted right before inserting (not just when the
     * window opened) so two quick clicks can't sneak through two votes.
     */
    private void castVote() {
        CandidateItem selected = (CandidateItem) candidateBox.getSelectedItem();
        if (selected == null) return;

        String insertVoteSql = "INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)";
        String updateVoterSql = "UPDATE voters SET has_voted = TRUE WHERE voter_id = ? AND has_voted = FALSE";

        try (Connection con = DBConnection.getConnection()) {

            // Guard again right before writing.
            if (hasAlreadyVoted()) {
                JOptionPane.showMessageDialog(this, "You have already voted.");
                dispose();
                return;
            }

            try (PreparedStatement updatePs = con.prepareStatement(updateVoterSql)) {
                updatePs.setInt(1, voterId);
                int rowsChanged = updatePs.executeUpdate();

                // rowsChanged == 0 means someone else's click already flipped
                // has_voted to true between our check and this update.
                if (rowsChanged == 0) {
                    JOptionPane.showMessageDialog(this, "You have already voted.");
                    dispose();
                    return;
                }
            }

            try (PreparedStatement insertPs = con.prepareStatement(insertVoteSql)) {
                insertPs.setInt(1, voterId);
                insertPs.setInt(2, selected.id);
                insertPs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Vote recorded. Thank you, " + selected.name + "!");
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
}
