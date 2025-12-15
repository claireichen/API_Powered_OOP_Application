package org.example.view.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GenerationPanel extends JPanel {

    private final JTextField txtPrompt;
    private final JTextField txtGenre;
    private final JTextField txtMood;
    private final JButton btnGenerate;
    private final JButton btnCancel;
    private final JLabel lblStatus;
    private final JLabel lblLink; // clickable label

    public GenerationPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Prompt
        add(new JLabel("Prompt:"), gbc);
        txtPrompt = new JTextField(25);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(txtPrompt, gbc);

        // Genre
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        add(new JLabel("Genre:"), gbc);

        txtGenre = new JTextField(15);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(txtGenre, gbc);

        // Mood
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        add(new JLabel("Mood:"), gbc);

        txtMood = new JTextField(15);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(txtMood, gbc);

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnGenerate = new JButton("Generate Instrumental");
        btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);

        buttonRow.add(btnGenerate);
        buttonRow.add(btnCancel);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(buttonRow, gbc);

        // Status + link
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lblStatus = new JLabel("Status: idle");

        lblLink = new JLabel(" ");
        lblLink.setForeground(new Color(0, 102, 204));
        lblLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        statusRow.add(lblStatus);
        statusRow.add(new JLabel("|"));
        statusRow.add(lblLink);

        add(statusRow, gbc);
    }

    // --- Accessors ---

    public String getPrompt() {
        return txtPrompt.getText();
    }

    public String getGenre() {
        return txtGenre.getText();
    }

    public String getMood() {
        return txtMood.getText();
    }

    // --- Button wiring ---

    public void onGenerate(ActionListener listener) {
        btnGenerate.addActionListener(listener);
    }

    public void onCancel(ActionListener listener) {
        btnCancel.addActionListener(listener);
    }

    public void onOpenLink(ActionListener listener) {
        lblLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                listener.actionPerformed(null);
            }
        });
    }

    public void setBusy(boolean busy) {
        btnGenerate.setEnabled(!busy);
        btnCancel.setEnabled(busy);
    }

    // --- UI updates from controller ---

    public void setStatusText(String status) {
        lblStatus.setText("Status: " + status);
    }

    public void setLinkText(String text) {
        lblLink.setText(text != null ? text : " ");
    }
}
