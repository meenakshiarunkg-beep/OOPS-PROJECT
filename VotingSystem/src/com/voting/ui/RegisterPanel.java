package com.voting.ui;

import com.voting.dao.VoterDAO;
import com.voting.util.PasswordUtil;
import com.voting.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RegisterPanel extends JPanel {

    private static final String[] SECURITY_QUESTIONS = {
            "What is your mother's maiden name?",
            "What was the name of your first school?",
            "What is the name of your favourite teacher?",
            "What was your childhood pet's name?",
            "In which city were you born?"
    };

    private final MainApp app;
    private final VoterDAO voterDAO = new VoterDAO();

    public RegisterPanel(MainApp app) {
        this.app = app;
        setBackground(UITheme.BACKGROUND);

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JLabel title = UITheme.heading("Voter Registration");
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Create your account to take part in the election");
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 700));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.weightx = 1;
        int row = 0;

        JTextField nameField = UITheme.textField();
        JTextField codeField = UITheme.textField();
        JTextField emailField = UITheme.textField();
        JSpinner dobSpinner = buildDobSpinner();
        JPasswordField pwField = UITheme.passwordField();
        JPasswordField pwConfirmField = UITheme.passwordField();
        JComboBox<String> questionBox = new JComboBox<>(SECURITY_QUESTIONS);
        questionBox.setFont(UITheme.FONT_LABEL);
        JTextField answerField = UITheme.textField();

        row = addField(card, gc, row, "Full Name", nameField);
        row = addField(card, gc, row, "Voter ID (e.g. college roll no.)", codeField);
        row = addField(card, gc, row, "Email Address", emailField);
        row = addFieldComponent(card, gc, row, "Date of Birth", dobSpinner);
        row = addField(card, gc, row, "Password (min 8 chars, letters+numbers)", pwField);
        row = addField(card, gc, row, "Confirm Password", pwConfirmField);
        row = addFieldComponent(card, gc, row, "Security Question (for password recovery)", questionBox);
        row = addField(card, gc, row, "Your Answer", answerField);

        gc.gridy = row++;
        gc.insets = new Insets(16, 6, 6, 6);
        JButton registerBtn = UITheme.primaryButton("Create Account");
        card.add(registerBtn, gc);

        gc.gridy = row++;
        gc.insets = new Insets(4, 6, 6, 6);
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setOpaque(false);
        backRow.add(new JLabel("Already registered?"));
        JButton backBtn = UITheme.linkButton("Back to login");
        backBtn.addActionListener(e -> app.showLogin());
        backRow.add(backBtn);
        card.add(backRow, gc);

        registerBtn.addActionListener(e -> handleRegister(
                nameField.getText().trim(),
                codeField.getText().trim(),
                emailField.getText().trim(),
                (Date) dobSpinner.getValue(),
                new String(pwField.getPassword()),
                new String(pwConfirmField.getPassword()),
                (String) questionBox.getSelectedItem(),
                answerField.getText().trim()
        ));

        wrapper.add(title);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(subtitle);
        wrapper.add(Box.createVerticalStrut(20));
        wrapper.add(card);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
    }

    private JSpinner buildDobSpinner() {
        Calendar min = new GregorianCalendar(1900, Calendar.JANUARY, 1);
        Calendar max = Calendar.getInstance();
       
        SpinnerDateModel model = new SpinnerDateModel(max.getTime(), min.getTime(), max.getTime(), Calendar.YEAR);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd-MM-yyyy");
        spinner.setEditor(editor);
        spinner.setFont(UITheme.FONT_LABEL);
        return spinner;
    }

    private int addField(JPanel card, GridBagConstraints gc, int row, String label, JComponent field) {
        return addFieldComponent(card, gc, row, label, field);
    }

    private int addFieldComponent(JPanel card, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row;
        gc.gridwidth = 2;
        card.add(UITheme.fieldLabel(label), gc);
        row++;
        gc.gridy = row;
        field.setPreferredSize(new Dimension(200, 34));
        card.add(field, gc);
        row++;
        return row;
    }

    private void handleRegister(String name, String code, String email, Date dobDate,
                                 String password, String confirmPassword,
                                 String securityQuestion, String securityAnswer) {

        if (ValidationUtil.isBlank(name) || ValidationUtil.isBlank(code) || ValidationUtil.isBlank(email)) {
            error("Please fill in your name, voter ID and email.");
            return;
        }
        if (!ValidationUtil.isValidVoterCode(code)) {
            error("Voter ID should be 3-20 letters/numbers, no spaces.");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            error("Please enter a valid email address.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            error("Password and confirmation do not match.");
            return;
        }
        if (!PasswordUtil.isStrong(password)) {
            error("Password must be at least 8 characters and include both letters and numbers.");
            return;
        }
        if (ValidationUtil.isBlank(securityAnswer)) {
            error("Please answer the security question - it's needed to recover your password later.");
            return;
        }

        LocalDate dob = dobDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String result = voterDAO.register(name, code, email, dob, password, securityQuestion, securityAnswer);
        if (result == null) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now log in with your Voter ID and password.",
                    "Welcome", JOptionPane.INFORMATION_MESSAGE);
            app.showLogin();
        } else {
            error(result);
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration failed", JOptionPane.ERROR_MESSAGE);
    }
}
