package voting;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;

/**
 * ResultsFrame
 * -------------
 * Shows every candidate with their vote count, sorted highest first,
 * and a label naming the winner (or "It's a tie" if the top count is shared).
 *
 * LEFT JOIN is used so a candidate with zero votes still shows up with 0,
 * instead of disappearing from the list.
 */
public class ResultsFrame extends JFrame {

    public ResultsFrame() {
        setTitle("Election Results");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columns = {"Candidate", "Party", "Votes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // results are read-only
            }
        };

        String winnerText = "No votes cast yet.";
        String sql = "SELECT c.name, c.party, COUNT(v.vote_id) AS vote_count " +
                     "FROM candidates c " +
                     "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                     "GROUP BY c.candidate_id, c.name, c.party " +
                     "ORDER BY vote_count DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int topVotes = -1;
            java.util.List<String> leaders = new java.util.ArrayList<>();

            while (rs.next()) {
                String name = rs.getString("name");
                String party = rs.getString("party");
                int votes = rs.getInt("vote_count");

                model.addRow(new Object[]{name, party, votes});

                if (votes > topVotes) {
                    topVotes = votes;
                    leaders.clear();
                    leaders.add(name);
                } else if (votes == topVotes) {
                    leaders.add(name);
                }
            }

            if (topVotes > 0) {
                if (leaders.size() == 1) {
                    winnerText = "Winner: " + leaders.get(0) + " (" + topVotes + " votes)";
                } else {
                    winnerText = "It's a tie between " + String.join(" and ", leaders)
                            + " with " + topVotes + " votes each";
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }

        JTable table = new JTable(model);
        JLabel winnerLabel = new JLabel(winnerText, JLabel.CENTER);
        winnerLabel.setFont(winnerLabel.getFont().deriveFont(Font.BOLD, 14f));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(winnerLabel, BorderLayout.SOUTH);
    }
}
