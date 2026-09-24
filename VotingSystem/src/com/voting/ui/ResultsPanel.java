package com.voting.ui;

import com.voting.dao.CandidateDAO;
import com.voting.dao.ElectionDAO;
import com.voting.model.Candidate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ResultsPanel extends JPanel {

    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final ElectionDAO electionDAO = new ElectionDAO();

    private final JTextField titleField = UITheme.textField();
    private final JLabel statusLabel = new JLabel();
    private final JButton toggleBtn = new JButton();
    private final BarChartPanel chartPanel = new BarChartPanel();

    public ResultsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 0, 0, 0));

        add(buildSettingsBar(), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);

        JPanel chartCard = UITheme.card();
        chartCard.setLayout(new BorderLayout());
        JLabel chartTitle = UITheme.fieldLabel("Live Results (visible to admin only while voting is open)");
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartPanel.setPreferredSize(new Dimension(600, 360));
        chartCard.add(chartPanel, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Results");
        refreshBtn.addActionListener(e -> refresh());
        JPanel refreshRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshRow.setOpaque(false);
        refreshRow.add(refreshBtn);
        chartCard.add(refreshRow, BorderLayout.SOUTH);

        centerWrap.add(chartCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildSettingsBar() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        card.add(UITheme.fieldLabel("Election Title:"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        titleField.setPreferredSize(new Dimension(260, 32));
        card.add(titleField, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        JButton saveTitleBtn = UITheme.primaryButton("Save Title");
        saveTitleBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            if (!t.isEmpty()) {
                electionDAO.setElectionTitle(t);
                JOptionPane.showMessageDialog(this, "Election title updated.");
            }
        });
        card.add(saveTitleBtn, gc);

        gc.gridx = 3;
        gc.insets = new Insets(4, 24, 4, 6);
        card.add(statusLabel, gc);

        gc.gridx = 4;
        gc.insets = new Insets(4, 6, 4, 6);
        toggleBtn.addActionListener(e -> {
            boolean currentlyOpen = electionDAO.isVotingOpen();
            String confirmMsg = currentlyOpen
                    ? "Close voting now? Voters will no longer be able to cast votes."
                    : "Open voting now? Voters will be able to start casting votes.";
            int choice = JOptionPane.showConfirmDialog(this, confirmMsg, "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                electionDAO.setVotingOpen(!currentlyOpen);
                refresh();
            }
        });
        card.add(toggleBtn, gc);

        return card;
    }

    private void refresh() {
        titleField.setText(electionDAO.getElectionTitle());
        boolean open = electionDAO.isVotingOpen();
        statusLabel.setText(open ? "Voting status: OPEN" : "Voting status: CLOSED");
        statusLabel.setForeground(open ? UITheme.ACCENT : UITheme.DANGER);
        statusLabel.setFont(UITheme.FONT_LABEL_BOLD);
        toggleBtn.setText(open ? "Close Voting" : "Open Voting");

        List<Candidate> candidates = candidateDAO.findAll();
        chartPanel.setCandidates(candidates);
    }

    /** Simple hand-drawn horizontal bar chart - no external charting library needed. */
    private static class BarChartPanel extends JPanel {
        private List<Candidate> candidates = List.of();

        void setCandidates(List<Candidate> candidates) {
            this.candidates = candidates;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (candidates.isEmpty()) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString("No candidates yet.", 20, 30);
                g2.dispose();
                return;
            }

            int maxVotes = candidates.stream().mapToInt(Candidate::getVoteCount).max().orElse(0);
            int totalVotes = candidates.stream().mapToInt(Candidate::getVoteCount).sum();
            int barMax = Math.max(maxVotes, 1);

            int top = 20;
            int rowHeight = Math.max(36, (getHeight() - 40) / Math.max(candidates.size(), 1));
            int labelWidth = 170;
            int rightMargin = 90;
            int barAreaWidth = getWidth() - labelWidth - rightMargin;

            Color[] palette = {
                    UITheme.PRIMARY, UITheme.ACCENT, UITheme.WARNING,
                    new Color(0x6C3EA6), new Color(0xC0392B), new Color(0x0F8FA9)
            };

            int y = top;
            for (int i = 0; i < candidates.size(); i++) {
                Candidate c = candidates.get(i);
                int barHeight = Math.min(26, rowHeight - 10);
                int barWidth = (int) (((double) c.getVoteCount() / barMax) * barAreaWidth);

                g2.setColor(UITheme.TEXT_DARK);
                g2.setFont(UITheme.FONT_LABEL_BOLD);
                String label = c.getFullName();
                if (label.length() > 20) label = label.substring(0, 18) + "...";
                g2.drawString(label, 4, y + barHeight - 6);

                g2.setColor(palette[i % palette.length]);
                g2.fillRoundRect(labelWidth, y, Math.max(barWidth, 2), barHeight, 8, 8);

                double pct = totalVotes == 0 ? 0 : (100.0 * c.getVoteCount() / totalVotes);
                g2.setColor(UITheme.TEXT_DARK);
                g2.setFont(UITheme.FONT_LABEL);
                g2.drawString(c.getVoteCount() + " (" + String.format("%.1f", pct) + "%)",
                        labelWidth + Math.max(barWidth, 2) + 8, y + barHeight - 6);

                y += rowHeight;
            }

            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString("Total votes cast: " + totalVotes, 4, getHeight() - 6);

            g2.dispose();
        }
    }
}
