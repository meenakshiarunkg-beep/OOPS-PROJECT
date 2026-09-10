package voter;

// Swing = the toolkit for building windows/buttons/text boxes in Java.
// We import the pieces we need instead of importing everything.
import javax.swing.JFrame;      // JFrame = a window
import javax.swing.JButton;     // JButton = a clickable button
import javax.swing.JLabel;      // JLabel = a piece of text on screen
import javax.swing.JOptionPane; // JOptionPane = the little popup dialog boxes
import java.awt.GridLayout;     // GridLayout = arranges things in rows/columns automatically
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;   // Person 1's connection helper
import voting.VotingFrame;      // the voting screen (also written by you)
import voting.ResultsFrame;     // the results screen (also written by you)

/**
 * VoterDashboard
 * ----------------
 * This is the screen shown right after a voter logs in successfully.
 * Person 2's LoginFrame should do:
 *      new VoterDashboard(loggedInVoterId, loggedInUsername).setVisible(true);
 *      this.dispose(); // close the login window
 *
 * "extends JFrame" means: this class IS a window. We don't have to build
 * a window from scratch every time — JFrame already knows how to be a window,
 * we just add stuff to it.
 */
public class VoterDashboard extends JFrame {

    // These two fields remember WHICH voter is currently logged in.
    // Every other screen (VotingFrame, ResultsFrame) needs to know this,
    // so we store it here and pass it along.
    private int voterId;
    private String username;

    // Constructor: runs automatically when you write "new VoterDashboard(...)"
    public VoterDashboard(int voterId, String username) {
        this.voterId = voterId;
        this.username = username;

        // --- Basic window setup ---
        setTitle("Voter Dashboard - Welcome " + username);
        setSize(400, 300);
        // EXIT_ON_CLOSE closes the whole program when this window's X is clicked.
        // If you don't want that (e.g. it should go back to login), use DISPOSE_ON_CLOSE instead.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // opens the window centered on screen

        // GridLayout(rows, columns) — 4 rows, 1 column, one item per row.
        setLayout(new GridLayout(5, 1, 5, 5));

        // --- Create the components ---
        JLabel welcomeLabel = new JLabel("Welcome, " + username, JLabel.CENTER);
        JButton viewCandidatesBtn = new JButton("View Candidates");
        JButton castVoteBtn = new JButton("Cast Vote");
        JButton viewStatusBtn = new JButton("View Status");
        JButton logoutBtn = new JButton("Logout");

        // --- Wire up what happens when each button is clicked ---
        // "addActionListener" registers a piece of code that runs on click.
        // We use a "lambda" (the -> arrow) which is just a short way of
        // writing "when clicked, run this code".

        viewCandidatesBtn.addActionListener(e -> viewCandidates());

        castVoteBtn.addActionListener(e -> {
            // Open the voting screen, passing along who is voting.
            new VotingFrame(voterId).setVisible(true);
        });

        viewStatusBtn.addActionListener(e -> viewStatus());

        logoutBtn.addActionListener(e -> {
            // Close this window. You can also open the LoginFrame again here
            // if Person 2 wants that behaviour: new LoginFrame().setVisible(true);
            dispose();
        });

        // --- Add everything to the window in the order it should appear ---
        add(welcomeLabel);
        add(viewCandidatesBtn);
        add(castVoteBtn);
        add(viewStatusBtn);
        add(logoutBtn);
    }

    /**
     * Shows a simple popup listing all candidates.
     * Uses the SAME database connection method Person 1 wrote — never open
     * a second/different connection, always reuse DBConnection.getConnection().
     */
    private void viewCandidates() {
        StringBuilder sb = new StringBuilder();

        // try-with-resources: Connection and PreparedStatement are automatically
        // closed for us when the block ends, even if an error happens.
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT name, party FROM candidates");
             ResultSet rs = ps.executeQuery()) {

            // rs.next() moves to the next row; returns false when there are no more rows.
            while (rs.next()) {
                sb.append(rs.getString("name"))
                  .append(" (")
                  .append(rs.getString("party"))
                  .append(")\n");
            }

            if (sb.length() == 0) {
                sb.append("No candidates added yet.");
            }

        } catch (SQLException ex) {
            // Never let the app crash silently — always show the error so you can debug.
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Candidates", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows whether this voter has already voted, by checking the
     * has_voted column for their row in the voters table.
     */
    private void viewStatus() {
        String sql = "SELECT has_voted FROM voters WHERE voter_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // The "?" above is a placeholder. setInt(1, voterId) fills in the
            // FIRST "?" with voterId. This is called a PreparedStatement and
            // it protects against SQL injection — NEVER build SQL by
            // concatenating strings like "WHERE voter_id = " + voterId.
            ps.setInt(1, voterId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean hasVoted = rs.getBoolean("has_voted");
                    String msg = hasVoted ? "You have already voted." : "You have not voted yet.";
                    JOptionPane.showMessageDialog(this, msg);
                } else {
                    JOptionPane.showMessageDialog(this, "Voter record not found.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
}
