package com.voting.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Central place for colors, fonts and small styling helpers so every screen looks consistent. */
public final class UITheme {

    public static final Color PRIMARY = new Color(0x1F4E8C);
    public static final Color PRIMARY_DARK = new Color(0x15335C);
    public static final Color ACCENT = new Color(0x2E9E5B);
    public static final Color DANGER = new Color(0xC0392B);
    public static final Color WARNING = new Color(0xE08E0B);
    public static final Color BACKGROUND = new Color(0xF3F5F9);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(0xD8DEE9);
    public static final Color TEXT_DARK = new Color(0x22262B);
    public static final Color TEXT_MUTED = new Color(0x6B7280);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_LABEL_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_MONO_SMALL = new Font("Monospaced", Font.PLAIN, 12);

    private UITheme() {
    }

    public static void applyGlobalDefaults() {
        UIManager.put("control", BACKGROUND);
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Button.foreground", TEXT_DARK);
        UIManager.put("Button.background", CARD_BG);
        UIManager.put("Label.font", FONT_LABEL);
        UIManager.put("TextField.font", FONT_LABEL);
        UIManager.put("PasswordField.font", FONT_LABEL);
        UIManager.put("Table.font", FONT_LABEL);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("TabbedPane.font", FONT_LABEL_BOLD);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT, Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_LABEL);
        b.setForeground(PRIMARY);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
    b.setUI(new javax.swing.plaf.basic.BasicButtonUI());    
    b.setFont(FONT_BUTTON);
    b.setBackground(bg);
    b.setForeground(fg);

    b.setOpaque(true);
    b.setContentAreaFilled(true);
    b.setBorderPainted(true);
    b.setFocusPainted(false);

    b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            new EmptyBorder(10, 22, 10, 22)
    ));

    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}
    
    

    public static JTextField textField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        styleField(f);
        return f;
    }

    private static void styleField(JComponent f) {
        f.setFont(FONT_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SUBTITLE);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL_BOLD);
        l.setForeground(TEXT_DARK);
        return l;
    }

    /** A rounded white "card" panel used to frame forms. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(28, 32, 28, 32)));
        return p;
    }
}
