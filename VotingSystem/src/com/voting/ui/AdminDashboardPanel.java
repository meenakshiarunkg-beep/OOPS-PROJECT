package com.voting.ui;

import com.voting.dao.AdminDAO;
import com.voting.model.AdminUser;
import com.voting.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {

    private final MainApp app;
    private final AdminUser admin;
    private final AdminDAO adminDAO = new AdminDAO();

    public AdminDashboardPanel(MainApp app, AdminUser admin) {
        this.app = app;
        this.admin = admin;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(buildTopBar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("  Candidates  ", new ManageCandidatesPanel());
        tabs.addTab("  Voters  ", new ManageVotersPanel());
        tabs.addTab("  Results & Settings  ", new ResultsPanel());
        tabs.setBorder(new EmptyBorder(16, 24, 24, 24));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY_DARK);
        bar.setBorder(new EmptyBorder(16, 28, 16, 28));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(UITheme.FONT_TITLE.deriveFont(20f));
        title.setForeground(Color.WHITE);

        JLabel who = new JLabel("Signed in as " + admin.getUsername());
        who.setForeground(new Color(255, 255, 255, 210));
        who.setFont(UITheme.FONT_LABEL);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(who);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton changePwBtn = new JButton("Change Password");
        changePwBtn.setFocusPainted(false);
        changePwBtn.addActionListener(e -> changePassword());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> app.logout());

        right.add(changePwBtn);
        right.add(logoutBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void changePassword() {
        JPasswordField current = new JPasswordField();
        JPasswordField newPw = new JPasswordField();
        JPasswordField confirmPw = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Current password:"));
        panel.add(current);
        panel.add(new JLabel("New password (min 8 chars, letters+numbers):"));
        panel.add(newPw);
        panel.add(new JLabel("Confirm new password:"));
        panel.add(confirmPw);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Admin Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String currentPw = new String(current.getPassword());
        String pw1 = new String(newPw.getPassword());
        String pw2 = new String(confirmPw.getPassword());

        if (adminDAO.authenticate(admin.getUsername(), currentPw) == null) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pw1.equals(pw2)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!PasswordUtil.isStrong(pw1)) {
            JOptionPane.showMessageDialog(this,
                    "New password must be at least 8 characters and include letters and numbers.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        adminDAO.changePassword(admin.getAdminId(), pw1);
        JOptionPane.showMessageDialog(this, "Password updated successfully.", "Done",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
