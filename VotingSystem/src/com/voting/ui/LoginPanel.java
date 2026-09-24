package com.voting.ui;

import com.voting.dao.AdminDAO;
import com.voting.dao.VoterDAO;
import com.voting.model.AdminUser;
import com.voting.model.Voter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final MainApp app;
    private final VoterDAO voterDAO = new VoterDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    public LoginPanel(MainApp app) {
        this.app = app;
        setBackground(UITheme.BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JLabel title = UITheme.heading("\uD83D\uDDF3\uFE0F  Online Voting System");
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Secure. Simple. Transparent.");
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(440, 380));
        card.setMaximumSize(new Dimension(440, 420));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Voter Login", buildVoterLoginForm());
        tabs.addTab("Admin Login", buildAdminLoginForm());
        card.add(tabs, BorderLayout.CENTER);

        wrapper.add(title);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(subtitle);
        wrapper.add(Box.createVerticalStrut(24));
        wrapper.add(card);

        add(wrapper);
    }

    private JPanel buildVoterLoginForm() {
        JPanel form = new JPanel();
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(new EmptyBorder(20, 10, 10, 10));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField idField = UITheme.textField();
        JPasswordField pwField = UITheme.passwordField();

        form.add(UITheme.fieldLabel("Voter ID or Email"));
        form.add(Box.createVerticalStrut(4));
        form.add(limitHeight(idField));
        form.add(Box.createVerticalStrut(14));
        form.add(UITheme.fieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(limitHeight(pwField));
        form.add(Box.createVerticalStrut(18));

        JButton loginBtn = UITheme.primaryButton("Login as Voter");
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doVoterLogin(idField.getText().trim(), new String(pwField.getPassword())));
        form.add(loginBtn);

        form.add(Box.createVerticalStrut(10));

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JButton forgotBtn = UITheme.linkButton("Forgot password?");
        forgotBtn.addActionListener(e -> app.showForgotPassword());
        linkRow.add(forgotBtn);
        form.add(linkRow);

        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setOpaque(false);
        registerRow.add(new JLabel("New here?"));
        JButton registerBtn = UITheme.linkButton("Register to vote");
        registerBtn.addActionListener(e -> app.showRegister());
        registerRow.add(registerBtn);
        form.add(registerRow);

        // Enter key submits from either field
        idField.addActionListener(e -> pwField.requestFocusInWindow());
        pwField.addActionListener(e -> loginBtn.doClick());

        return form;
    }

    private JPanel buildAdminLoginForm() {
        JPanel form = new JPanel();
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(new EmptyBorder(28, 10, 10, 10));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField userField = UITheme.textField();
        JPasswordField pwField = UITheme.passwordField();

        form.add(UITheme.fieldLabel("Admin Username"));
        form.add(Box.createVerticalStrut(4));
        form.add(limitHeight(userField));
        form.add(Box.createVerticalStrut(14));
        form.add(UITheme.fieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(limitHeight(pwField));
        form.add(Box.createVerticalStrut(18));

        JButton loginBtn = UITheme.primaryButton("Login as Admin");
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doAdminLogin(userField.getText().trim(), new String(pwField.getPassword())));
        form.add(loginBtn);

        form.add(Box.createVerticalStrut(16));
        JLabel hint = new JLabel("<html><center>Default admin: <b>admin</b> / <b>Admin@123</b><br>" +
                "(change this after first login)</center></html>");
        hint.setFont(UITheme.FONT_LABEL);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);
        form.add(hint);

        userField.addActionListener(e -> pwField.requestFocusInWindow());
        pwField.addActionListener(e -> loginBtn.doClick());

        return form;
    }

    private Component limitHeight(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private void doVoterLogin(String identifier, String password) {
        if (identifier.isEmpty() || password.isEmpty()) {
            error("Please enter both your Voter ID/Email and password.");
            return;
        }
        VoterDAO.AuthResult result = voterDAO.authenticate(identifier, password);
        switch (result.getStatus()) {
            case SUCCESS:
                app.showVoterDashboard(result.getVoter());
                break;
            case LOCKED:
                error("This account is locked after too many failed login attempts.\n" +
                        "Use \"Forgot password?\" to reset it, or contact the election admin.");
                break;
            case WRONG_PASSWORD:
                error("Incorrect password. Please try again.");
                break;
            case NOT_FOUND:
            default:
                error("No voter found with that ID/email. Please check and try again, or register.");
        }
    }

    private void doAdminLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            error("Please enter both the admin username and password.");
            return;
        }
        AdminUser admin = adminDAO.authenticate(username, password);
        if (admin != null) {
            app.showAdminDashboard(admin);
        } else {
            error("Invalid admin username or password.");
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Login failed", JOptionPane.ERROR_MESSAGE);
    }
}
