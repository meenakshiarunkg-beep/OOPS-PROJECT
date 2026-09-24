package com.voting.ui;

import com.voting.dao.CandidateDAO;
import com.voting.dao.ElectionDAO;
import com.voting.dao.VoteDAO;
import com.voting.model.Candidate;
import com.voting.model.Voter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class VoterDashboardPanel extends JPanel {

    private final MainApp app;
    private final Voter voter;
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final VoteDAO voteDAO = new VoteDAO();
    private final ElectionDAO electionDAO = new ElectionDAO();

    private final JPanel bodyPanel = new JPanel();

    public VoterDashboardPanel(MainApp app, Voter voter) {
        this.app = app;
        this.voter = voter;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(buildTopBar(), BorderLayout.NORTH);

        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(24, 40, 24, 40));
        bodyPanel.setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY);
        bar.setBorder(new EmptyBorder(16, 28, 16, 28));

        JLabel title = new JLabel(electionDAO.getElectionTitle());
        title.setFont(UITheme.FONT_TITLE.deriveFont(20f));
        title.setForeground(Color.WHITE);

        JLabel who = new JLabel("Signed in as " + voter.getFullName() + " (" + voter.getVoterCode() + ")");
        who.setForeground(new Color(255, 255, 255, 210));
        who.setFont(UITheme.FONT_LABEL);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(who);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> app.logout());

        bar.add(left, BorderLayout.WEST);
        bar.add(logoutBtn, BorderLayout.EAST);
        return bar;
    }

    private void refresh() {
        bodyPanel.removeAll();

        boolean votingOpen = electionDAO.isVotingOpen();

        if (voter.isHasVoted()) {
            bodyPanel.add(buildAlreadyVotedView(votingOpen), BorderLayout.CENTER);
        } else if (!votingOpen) {
            bodyPanel.add(buildVotingClosedView(), BorderLayout.CENTER);
        } else {
            bodyPanel.add(buildBallot(), BorderLayout.CENTER);
        }

        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    private JComponent buildBallot() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel heading = UITheme.heading("Cast Your Vote");
        JLabel sub = UITheme.subtitle("Choose one candidate below. You can only vote once.");
        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(sub);
        wrap.add(Box.createVerticalStrut(18));

        List<Candidate> candidates = candidateDAO.findAll();
        if (candidates.isEmpty()) {
            JLabel none = UITheme.subtitle("No candidates have been added yet. Please check back later.");
            wrap.add(none);
            return wrap;
        }

        for (Candidate c : candidates) {
            wrap.add(buildCandidateCard(c));
            wrap.add(Box.createVerticalStrut(12));
        }
        return wrap;
    }

    private JPanel buildCandidateCard(Candidate c) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(16, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(new AvatarIcon(c.getPhotoPath(), c.getFullName(), 64));
        card.add(avatar, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(c.getFullName());
        name.setFont(UITheme.FONT_LABEL_BOLD.deriveFont(16f));
        JLabel party = new JLabel(c.getParty() + "  \u00B7  " + c.getPositionName());
        party.setForeground(UITheme.TEXT_MUTED);
        party.setFont(UITheme.FONT_LABEL);
        info.add(name);
        info.add(party);
        card.add(info, BorderLayout.CENTER);

        JButton voteBtn = UITheme.accentButton("Vote");
        voteBtn.addActionListener(e -> confirmAndVote(c));
        JPanel btnWrap = new JPanel(new GridBagLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(voteBtn);
        card.add(btnWrap, BorderLayout.EAST);

        return card;
    }

    private void confirmAndVote(Candidate c) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Vote for " + c.getFullName() + " (" + c.getParty() + ")?\nThis cannot be undone.",
                "Confirm your vote", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        VoteDAO.VoteResult result = voteDAO.castVote(voter.getVoterId(), c.getCandidateId());
        switch (result) {
            case SUCCESS:
                voter.setHasVoted(true);
                JOptionPane.showMessageDialog(this,
                        "Your vote has been recorded. Thank you for participating!",
                        "Vote cast", JOptionPane.INFORMATION_MESSAGE);
                refresh();
                break;
            case ALREADY_VOTED:
                voter.setHasVoted(true);
                JOptionPane.showMessageDialog(this, "Our records show you have already voted.",
                        "Already voted", JOptionPane.WARNING_MESSAGE);
                refresh();
                break;
            case VOTING_CLOSED:
                JOptionPane.showMessageDialog(this, "Voting has just been closed by the administrator.",
                        "Voting closed", JOptionPane.WARNING_MESSAGE);
                refresh();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Something went wrong casting your vote. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent buildAlreadyVotedView(boolean votingOpen) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(520, 200));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel icon = new JLabel("\u2705");
        icon.setFont(icon.getFont().deriveFont(40f));
        JLabel msg = UITheme.heading("You've already voted");
        JLabel sub = UITheme.subtitle(votingOpen
                ? "Results will be published once voting closes."
                : "Voting has closed. Thank you for participating.");

        card.add(icon);
        card.add(Box.createVerticalStrut(8));
        card.add(msg);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);

        wrap.add(card);
        return wrap;
    }

    private JComponent buildVotingClosedView() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(520, 160));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel msg = UITheme.heading("Voting is currently closed");
        JLabel sub = UITheme.subtitle("Please check back once the administrator opens the election.");
        card.add(msg);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);

        wrap.add(card);
        return wrap;
    }
}
