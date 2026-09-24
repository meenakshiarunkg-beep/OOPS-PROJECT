package com.voting.ui;

import com.voting.dao.VoterDAO;
import com.voting.model.Voter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ManageVotersPanel extends JPanel {

    private final VoterDAO voterDAO = new VoterDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public ManageVotersPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton unlockBtn = UITheme.primaryButton("Unlock Selected");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        toolbar.add(unlockBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Voter ID", "Email", "Has Voted", "Locked"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        unlockBtn.addActionListener(e -> unlockSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadVoters());

        loadVoters();
    }

    private void loadVoters() {
        tableModel.setRowCount(0);
        for (Voter v : voterDAO.findAll()) {
            tableModel.addRow(new Object[]{
                    v.getVoterId(), v.getFullName(), v.getVoterCode(), v.getEmail(),
                    v.isHasVoted() ? "Yes" : "No",
                    v.isLocked() ? "LOCKED" : "No"
            });
        }
    }

    private int getSelectedVoterId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return (int) tableModel.getValueAt(row, 0);
    }

    private void unlockSelected() {
        int id = getSelectedVoterId();
        if (id < 0) {
            warn("Select a voter first.");
            return;
        }
        if (voterDAO.unlockVoter(id)) {
            loadVoters();
            JOptionPane.showMessageDialog(this, "Account unlocked.", "Done", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not unlock this account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int id = getSelectedVoterId();
        if (id < 0) {
            warn("Select a voter first.");
            return;
        }
        int row = table.getSelectedRow();
        String name = (String) tableModel.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Permanently delete voter \"" + name + "\"? Their vote (if any) will also be removed.\n" +
                        "This cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        if (voterDAO.deleteVoter(id)) {
            loadVoters();
        } else {
            JOptionPane.showMessageDialog(this, "Could not delete this voter.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}
