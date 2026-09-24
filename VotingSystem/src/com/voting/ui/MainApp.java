package com.voting.ui;

import com.voting.dao.AdminDAO;
import com.voting.model.AdminUser;
import com.voting.model.Voter;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Single JFrame whose content pane is a CardLayout. Every screen is a
 * JPanel that gets swapped in/out; screens navigate by calling back into
 * this class instead of opening new windows, which keeps the whole app
 * feeling like one continuous application.
 */
public class MainApp extends JFrame {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_REGISTER = "REGISTER";
    private static final String CARD_FORGOT = "FORGOT";
    private static final String CARD_ADMIN = "ADMIN";
    private static final String CARD_VOTER = "VOTER";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, JComponent> currentCard = new HashMap<>();

    public MainApp() {
        super("Online Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        cards.setBackground(UITheme.BACKGROUND);
        add(cards);

        putCard(CARD_LOGIN, new LoginPanel(this));
        putCard(CARD_REGISTER, new RegisterPanel(this));
        putCard(CARD_FORGOT, new ForgotPasswordPanel(this));

        showLogin();
    }

    /** Adds/replaces the panel registered under a given card name. */
    private void putCard(String name, JComponent panel) {
        JComponent old = currentCard.get(name);
        if (old != null) cards.remove(old);
        cards.add(panel, name);
        currentCard.put(name, panel);
    }

    // ------------------------------- Navigation -------------------------------

    public void showLogin() {
        putCard(CARD_LOGIN, new LoginPanel(this)); // fresh screen -> clears typed text
        cardLayout.show(cards, CARD_LOGIN);
    }

    public void showRegister() {
        putCard(CARD_REGISTER, new RegisterPanel(this));
        cardLayout.show(cards, CARD_REGISTER);
    }

    public void showForgotPassword() {
        putCard(CARD_FORGOT, new ForgotPasswordPanel(this));
        cardLayout.show(cards, CARD_FORGOT);
    }

    public void showAdminDashboard(AdminUser admin) {
        putCard(CARD_ADMIN, new AdminDashboardPanel(this, admin));
        cardLayout.show(cards, CARD_ADMIN);
    }

    public void showVoterDashboard(Voter voter) {
        putCard(CARD_VOTER, new VoterDashboardPanel(this, voter));
        cardLayout.show(cards, CARD_VOTER);
    }

    public void logout() {
        showLogin();
    }

    // ------------------------------- Entry point -------------------------------

    public static void main(String[] args) {
      try {
    UIManager.setLookAndFeel(
        UIManager.getSystemLookAndFeelClassName()
    );
} catch (Exception ignored) {
}

UITheme.applyGlobalDefaults();

        // Make sure a default admin account exists before the UI opens.
        new AdminDAO().bootstrapDefaultAdminIfNeeded();

        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}
