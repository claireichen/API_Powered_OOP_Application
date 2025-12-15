package org.example.view.panels;

import org.example.controller.MainController;
import org.example.model.domain.GenerationResult;
import org.example.model.domain.UserQuery;
import org.example.service.MusicServiceFactory.GenerationMode;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class GenerationPanel extends JPanel {

    private final MainController controller;

    private final JTextField txtPrompt = new JTextField(20);
    private final JTextField txtGenre = new JTextField(10);
    private final JTextField txtMood = new JTextField(10);
    private final JButton btnGenerate = new JButton("Generate Instrumental");
    private final JLabel  lblResult = new JLabel("No generation yet.");
    private String lastAudioUrl;   // NEW

    public GenerationPanel(MainController controller) {
        this.controller = controller;
        initLayout();
        initActions();
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Prompt:"), gbc);
        gbc.gridx = 1;
        add(txtPrompt, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        add(txtGenre, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Mood:"), gbc);
        gbc.gridx = 1;
        add(txtMood, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(btnGenerate, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(lblResult, gbc);
    }

    private void initActions() {
        btnGenerate.addActionListener(e -> {
            UserQuery query = new UserQuery()
                    .setText(txtPrompt.getText())
                    .setGenre(txtGenre.getText())
                    .setMood(txtMood.getText());

            lastAudioUrl = null;
            lblResult.setText("Starting generation...");
            controller.requestGeneration(query, GenerationMode.INSTRUMENTAL);
        });

        // Optional: click the label to open the URL
        lblResult.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (lastAudioUrl != null && Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(lastAudioUrl));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void updateGenerationResult(Object payload) {
        if (payload instanceof GenerationResult result) {
            String status = result.getStatus();
            String url = result.getAudioUrl();
            lastAudioUrl = url;

            if (url != null && !url.isBlank()) {
                lblResult.setText("<html>Status: " + status +
                        " | <u>Click here to open audio</u></html>");
            } else {
                lblResult.setText("Status: " + status + " (no audio URL)");
            }
        }
    }
}
