package org.example.view.panels;

import org.example.controller.MainController;
import org.example.model.domain.UserQuery;
import org.example.service.MusicServiceFactory.RecommendationMode;
import org.example.model.domain.Session;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFileChooser;

public class SearchPanel extends JPanel {

    private final MainController controller;

    private final JTextField txtQuery = new JTextField(20);
    private final JTextField txtMood = new JTextField(10);
    private final JTextField txtGenre = new JTextField(10);
    private final JTextField txtArtist = new JTextField(10);

    private final JComboBox<RecommendationMode> cmbMode =
            new JComboBox<>(RecommendationMode.values());

    private final JButton btnRecommend = new JButton("Get Recommendations");
    private final JButton btnSave = new JButton("Save Session");
    private final JButton btnLoad = new JButton("Load Session");

    public SearchPanel(MainController controller) {
        this.controller = controller;
        initLayout();
        initActions();
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Query:"), gbc);
        gbc.gridx = 1;
        add(txtQuery, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Mood:"), gbc);
        gbc.gridx = 1;
        add(txtMood, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        add(txtGenre, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Artist:"), gbc);
        gbc.gridx = 1;
        add(txtArtist, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Mode:"), gbc);
        gbc.gridx = 1;
        add(cmbMode, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(btnRecommend, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        add(btnSave, gbc);

        gbc.gridx = 1; gbc.gridy = 6;
        add(btnLoad, gbc);
    }

    private void initActions() {
        btnRecommend.addActionListener(e -> {
            UserQuery query = new UserQuery()
                    .setText(txtQuery.getText())
                    .setMood(txtMood.getText())
                    .setGenre(txtGenre.getText())
                    .setArtist(txtArtist.getText());

            RecommendationMode mode = (RecommendationMode) cmbMode.getSelectedItem();
            controller.requestRecommendations(query, mode);
        });

        btnSave.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                controller.saveCurrentSession(chooser.getSelectedFile());
            }
        });

        btnLoad.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                controller.loadSession(chooser.getSelectedFile());
            }
        });
    }

    public void applyLoadedSession(Session session) {
        if (session == null || session.getQuery() == null) {
            return;
        }
        UserQuery q = session.getQuery();
        txtQuery.setText(q.getText());
        txtMood.setText(q.getMood());
        txtGenre.setText(q.getGenre());
        txtArtist.setText(q.getArtist());
    }
}
