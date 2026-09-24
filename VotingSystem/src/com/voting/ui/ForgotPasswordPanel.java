package com.voting.ui;

import com.voting.dao.VoterDAO;
import com.voting.model.Voter;
import com.voting.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A 4-step identity-verification wizard, built as an inner CardLayout:
 *   1. Identify the account (Voter ID or email)
 *   2. Confirm date of birth on file
 *   3. Answer the security question chosen at registration
 *   4. Set a new password
 *
 * Three combined failures across steps 2-3 locks the account, mirroring
 * how the normal login lockout works, and pushes the user back to Login.
 */
public class ForgotPasswordPanel extends JPanel {

    private static final int MAX_ATTEMPTS = 3;

    private final MainApp app;
    private final VoterDAO voterDAO = new VoterDAO();

    private final CardLayout stepLayout = new CardLayout();
    private final JPanel stepPanel = new JPanel(stepLayout);

    private Voter targetVoter;
    private int attemptsLeft = MAX_ATTEMPTS;

    private final JLabel progressLabel = UITheme.subtitle("Step 1 of 4");

    public ForgotPasswordPanel(MainApp app) {
        this.app = app;
        setBackground(UITheme.BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JLabel title = UITheme.heading("Reset Your Password");
        title.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);

        JPanel card = UITheme.card();
        card.setPreferredSize(new Dimension(460, 380));
        card.setLayout(new BorderLayout());

        stepPanel.setOpaque(false);
        stepPanel.add(buildStep1(), "1");
        stepPanel.add(buildStep2(), "2");
        stepPanel.add(buildStep3(), "3");
        stepPanel.add(buildStep4(), "4");
        card.add(stepPanel, BorderLayout.CENTER);

        wrapper.add(title);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(progressLabel);
        wrapper.add(Box.createVerticalStrut(20));
        wrapper.add(card);
        wrapper.add(Box.createVerticalStrut(14));

        JButton backToLogin = UITheme.linkButton("Cancel and back to login");
        backToLogin.setAlignmentX(CENTER_ALIGNMENT);
        backToLogin.addActionListener(e -> app.showLogin());
        wrapper.add(backToLogin);

        add(wrapper);
    }

    // ---------------------------------------------------------------- Step 1

    private JPanel buildStep1() {
        JPanel p = formPanel();
        JTextField idField = UITheme.textField();

        p.add(UITheme.fieldLabel("Enter your Voter ID or Email"));
        p.add(Box.createVerticalStrut(6));
        p.add(limitHeight(idField));
        p.add(Box.createVerticalStrut(20));

        JButton next = UITheme.primaryButton("Find My Account");
        next.setAlignmentX(CENTER_ALIGNMENT);
        next.addActionListener(e -> {
            String identifier = idField.getText().trim();
            if (identifier.isEmpty()) {
                error("Please enter your Voter ID or email.");
                return;
            }
            Voter voter = voterDAO.findByCodeOrEmail(identifier);
            if (voter == null) {
                error("No account found with that Voter ID/email.");
                return;
            }
            if (voter.isLocked()) {
                error("This account is locked. Please contact the election administrator.");
                return;
            }
            targetVoter = voter;
            attemptsLeft = MAX_ATTEMPTS;
            goToStep(2);
        });
        p.add(next);
        idField.addActionListener(e -> next.doClick());
        return p;
    }

    // ---------------------------------------------------------------- Step 2

    private JSpinner dobSpinner;

    private JPanel buildStep2() {
        JPanel p = formPanel();
        p.add(UITheme.fieldLabel("Confirm your Date of Birth"));
        p.add(Box.createVerticalStrut(6));

        Calendar min = new GregorianCalendar(1900, Calendar.JANUARY, 1);
        Calendar max = Calendar.getInstance();
        SpinnerDateModel model = new SpinnerDateModel(max.getTime(), min.getTime(), max.getTime(), Calendar.YEAR);
        dobSpinner = new JSpinner(model);
        dobSpinner.setEditor(new JSpinner.DateEditor(dobSpinner, "dd-MM-yyyy"));
        dobSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        dobSpinner.setAlignmentX(LEFT_ALIGNMENT);
        p.add(dobSpinner);
        p.add(Box.createVerticalStrut(20));

        JButton next = UITheme.primaryButton("Verify");
        next.setAlignmentX(CENTER_ALIGNMENT);
        next.addActionListener(e -> {
            Date d = (Date) dobSpinner.getValue();
            LocalDate entered = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (voterDAO.verifyDob(targetVoter, entered)) {
                goToStep(3);
            } else {
                registerFailedAttempt("Date of birth does not match our records.");
            }
        });
        p.add(next);
        return p;
    }

    // ---------------------------------------------------------------- Step 3

    private JLabel questionLabel;

    private JPanel buildStep3() {
        JPanel p = formPanel();
        questionLabel = UITheme.fieldLabel("Security question");
        questionLabel.setText("");
        p.add(questionLabel);
        p.add(Box.createVerticalStrut(6));

        JTextField answerField = UITheme.textField();
        p.add(limitHeight(answerField));
        p.add(Box.createVerticalStrut(20));

        JButton next = UITheme.primaryButton("Verify Answer");
        next.setAlignmentX(CENTER_ALIGNMENT);
        next.addActionListener(e -> {
            if (voterDAO.verifySecurityAnswer(targetVoter, answerField.getText())) {
                goToStep(4);
            } else {
                registerFailedAttempt("That answer doesn't match what we have on file.");
            }
        });
        p.add(next);
        answerField.addActionListener(e -> next.doClick());

        // Refresh the displayed question every time we enter this card
        p.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (targetVoter != null) {
                    questionLabel.setText("<html>" + targetVoter.getSecurityQuestion() + "</html>");
                }
                answerField.setText("");
            }
        });
        return p;
    }

    // ---------------------------------------------------------------- Step 4

    private JPanel buildStep4() {
        JPanel p = formPanel();
        p.add(UITheme.fieldLabel("New Password (min 8 chars, letters+numbers)"));
        p.add(Box.createVerticalStrut(6));
        JPasswordField pw1 = UITheme.passwordField();
        p.add(limitHeight(pw1));
        p.add(Box.createVerticalStrut(14));
        p.add(UITheme.fieldLabel("Confirm New Password"));
        p.add(Box.createVerticalStrut(6));
        JPasswordField pw2 = UITheme.passwordField();
        p.add(limitHeight(pw2));
        p.add(Box.createVerticalStrut(20));

        JButton submit = UITheme.accentButton("Reset Password");
        submit.setAlignmentX(CENTER_ALIGNMENT);
        submit.addActionListener(e -> {
            String p1 = new String(pw1.getPassword());
            String p2 = new String(pw2.getPassword());
            if (!p1.equals(p2)) {
                error("Passwords do not match.");
                return;
            }
            if (!PasswordUtil.isStrong(p1)) {
                error("Password must be at least 8 characters with letters and numbers.");
                return;
            }
            boolean ok = voterDAO.resetPassword(targetVoter.getVoterId(), p1);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Your password has been reset. Please log in with your new password.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                app.showLogin();
            } else {
                error("Something went wrong saving your new password. Please try again.");
            }
        });
        p.add(submit);
        return p;
    }

    // -------------------------------------------------------------- helpers

    private void registerFailedAttempt(String message) {
        attemptsLeft--;
        if (attemptsLeft <= 0) {
            voterDAO.lockAccount(targetVoter.getVoterId());
            JOptionPane.showMessageDialog(this,
                    "Too many failed attempts. This account has been locked for security.\n" +
                            "Please contact the election administrator to unlock it.",
                    "Account locked", JOptionPane.ERROR_MESSAGE);
            app.showLogin();
        } else {
            error(message + "\n" + attemptsLeft + " attempt(s) remaining before this account is locked.");
        }
    }

    private void goToStep(int step) {
        progressLabel.setText("Step " + step + " of 4");
        stepLayout.show(stepPanel, String.valueOf(step));
    }

    private JPanel formPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        return p;
    }

    private Component limitHeight(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Verification failed", JOptionPane.ERROR_MESSAGE);
    }
}
