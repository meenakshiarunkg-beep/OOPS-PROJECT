package com.voting.ui;

import com.voting.dao.CandidateDAO;
import com.voting.model.Candidate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ManageCandidatesPanel extends JPanel {

    private static final String PHOTO_DIR = "candidate_photos";

    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public ManageCandidatesPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton addBtn = UITheme.accentButton("+ Add Candidate");
        JButton editBtn = UITheme.primaryButton("Edit Selected");
        JButton removeBtn = UITheme.dangerButton("Remove Selected");
        JButton refreshBtn = new JButton("Refresh");
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(removeBtn);
        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Party", "Position", "Votes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        addBtn.addActionListener(e -> openCandidateDialog(null));
        editBtn.addActionListener(e -> {
            Candidate selected = getSelectedCandidate();
            if (selected == null) {
                warn("Select a candidate to edit first.");
                return;
            }
            openCandidateDialog(selected);
        });
        removeBtn.addActionListener(e -> removeSelected());
        refreshBtn.addActionListener(e -> loadCandidates());

        loadCandidates();
    }

    private void loadCandidates() {
        tableModel.setRowCount(0);
        for (Candidate c : candidateDAO.findAll()) {
            tableModel.addRow(new Object[]{
                    c.getCandidateId(), c.getFullName(), c.getParty(), c.getPositionName(), c.getVoteCount()
            });
        }
    }

    private Candidate getSelectedCandidate() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        return candidateDAO.findById(id);
    }

    private void removeSelected() {
        Candidate selected = getSelectedCandidate();
        if (selected == null) {
            warn("Select a candidate to remove first.");
            return;
        }

        String warning = "Remove \"" + selected.getFullName() + "\" (" + selected.getParty() + ")?";
        if (selected.getVoteCount() > 0) {
            warning += "\n\nThis candidate has already received " + selected.getVoteCount() +
                    " vote(s). Removing them will permanently delete those votes too.";
        }
        warning += "\nThis cannot be undone.";

        int choice = JOptionPane.showConfirmDialog(this, warning, "Confirm removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        boolean ok = candidateDAO.removeCandidate(selected.getCandidateId());
        if (ok) {
            loadCandidates();
            JOptionPane.showMessageDialog(this, "Candidate removed.", "Done",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not remove this candidate. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Shared dialog for both "Add" (existing == null) and "Edit" (existing != null). */
    private void openCandidateDialog(Candidate existing) {
        JTextField nameField = new JTextField(existing != null ? existing.getFullName() : "");
        JTextField partyField = new JTextField(existing != null ? existing.getParty() : "");
        JTextField positionField = new JTextField(existing != null ? existing.getPositionName() : "General Seat");
        JLabel photoLabel = new JLabel(existing != null && existing.getPhotoPath() != null
                ? new File(existing.getPhotoPath()).getName() : "No photo selected");
        final String[] chosenPhotoPath = {existing != null ? existing.getPhotoPath() : null};

        JButton browseBtn = new JButton("Choose Photo...");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image files", "jpg", "jpeg", "png", "gif"));
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                try {
                    String stored = copyPhotoIntoProject(selectedFile);
                    chosenPhotoPath[0] = stored;
                    photoLabel.setText(new File(stored).getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not copy that image: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 6));
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Party / Group:"));
        form.add(partyField);
        form.add(new JLabel("Position / Seat:"));
        form.add(positionField);
        form.add(new JLabel("Photo (optional):"));
        JPanel photoRow = new JPanel(new BorderLayout(8, 0));
        photoRow.add(photoLabel, BorderLayout.CENTER);
        photoRow.add(browseBtn, BorderLayout.EAST);
        form.add(photoRow);
       
        String dialogTitle = (existing == null) ? "Add Candidate" : "Edit Candidate";
        int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String party = partyField.getText().trim();
        String position = positionField.getText().trim();

        if (name.isEmpty() || party.isEmpty() || position.isEmpty()) {
            warn("Name, party and position are all required.");
            return;
        }

        boolean ok;
        if (existing == null) {
            ok = candidateDAO.addCandidate(name, party, position, chosenPhotoPath[0]);
        } else {
            ok = candidateDAO.updateCandidate(existing.getCandidateId(), name, party, position, chosenPhotoPath[0]);
        }

        if (ok) {
            loadCandidates();
        } else {
            JOptionPane.showMessageDialog(this, "Could not save this candidate. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Copies the chosen image into ./candidate_photos so it survives independent of where the user picked it from. */
    private String copyPhotoIntoProject(File source) throws IOException {
        Path dir = Paths.get(PHOTO_DIR);
        Files.createDirectories(dir);
        String ext = "";
        int dot = source.getName().lastIndexOf('.');
        if (dot >= 0) ext = source.getName().substring(dot);
        String newName = "candidate_" + System.currentTimeMillis() + ext;
        Path dest = dir.resolve(newName);
        Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}
