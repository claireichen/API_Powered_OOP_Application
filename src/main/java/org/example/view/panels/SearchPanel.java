package org.example.view.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Left-side panel for recommendations: query + mode + buttons.
 * Exposes methods so MainFrame can attach listeners.
 */
public class SearchPanel extends JPanel {

    private final JTextField txtQuery;
    private final JComboBox<String> cmbMode;
    private final JButton btnSearch;
    private final JButton btnCancel;

    public SearchPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        // Query label + field
        add(new JLabel("Query / Mood / Artist:"), gbc);

        txtQuery = new JTextField(25);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(txtQuery, gbc);

        // Mode label + combo
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        add(new JLabel("Mode:"), gbc);

        cmbMode = new JComboBox<>(new String[] { "Mood", "Genre", "Artist" });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(cmbMode, gbc);

        // Buttons row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnSearch = new JButton("Get Recommendations");
        btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);

        buttonRow.add(btnSearch);
        buttonRow.add(btnCancel);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(buttonRow, gbc);
    }

    // --- Data accessors ---

    public String getQueryText() {
        return txtQuery.getText();
    }

    public void setQueryText(String text) {
        txtQuery.setText(text != null ? text : "");
    }

    public String getSelectedMode() {
        return (String) cmbMode.getSelectedItem();
    }

    public void setSelectedMode(String mode) {
        cmbMode.setSelectedItem(mode);
    }

    // --- Button wiring ---

    public void onSearch(ActionListener listener) {
        btnSearch.addActionListener(listener);
    }

    public void onCancel(ActionListener listener) {
        btnCancel.addActionListener(listener);
    }

    public void setBusy(boolean busy) {
        btnSearch.setEnabled(!busy);
        btnCancel.setEnabled(busy);
    }
}
