package org.example.view.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Simple status bar with message + indeterminate progress bar.
 * The controller / MainFrame calls setMessage + setBusy.
 */
public class StatusBar extends JPanel {

    private final JLabel messageLabel;
    private final JProgressBar progressBar;

    public StatusBar() {
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        setBackground(new Color(245, 245, 245));

        messageLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 14));
        progressBar.setBorderPainted(false);

        add(messageLabel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.EAST);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setBusy(boolean busy) {
        progressBar.setVisible(busy);
    }
}
