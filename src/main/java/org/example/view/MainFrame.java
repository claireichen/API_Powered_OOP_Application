package org.example.view;

import org.example.controller.MainController;
import org.example.controller.MusicEvent;
import org.example.controller.MusicEventListener;
import org.example.model.domain.Session;
import org.example.view.panels.GenerationPanel;
import org.example.view.panels.ResultPanel;
import org.example.view.panels.SearchPanel;
import org.example.view.panels.StatusBar;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements MusicEventListener {

    private final MainController controller;
    private final SearchPanel searchPanel;
    private final ResultPanel resultPanel;
    private final GenerationPanel generationPanel;
    private final StatusBar statusBar;

    public MainFrame(MainController controller) {
        super("MuseMix - Music Recommender & Generator");
        this.controller = controller;

        this.searchPanel = new SearchPanel(controller);
        this.resultPanel = new ResultPanel();
        this.generationPanel = new GenerationPanel(controller);
        this.statusBar = new StatusBar();

        controller.addListener(this);

        initLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Recommendations", searchPanel);
        tabs.addTab("Generation", generationPanel);

        add(tabs, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    @Override
    public void onMusicEvent(MusicEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case RECOMMENDATION_STARTED -> statusBar.setMessage("Fetching recommendations...");
                case RECOMMENDATION_COMPLETED -> {
                    statusBar.setMessage("Recommendations updated.");
                    resultPanel.updateResults(event.getPayload());
                }
                case GENERATION_STARTED -> statusBar.setMessage("Generating music...");
                case GENERATION_COMPLETED -> {
                    statusBar.setMessage("Generation complete.");
                    generationPanel.updateGenerationResult(event.getPayload());
                }
                case SESSION_SAVED -> statusBar.setMessage("Session saved.");
                case SESSION_LOADED -> {
                    statusBar.setMessage("Session loaded.");
                    if (event.getPayload() instanceof Session session) {
                        searchPanel.applyLoadedSession(session);
                        resultPanel.updateResults(session.getTracks());
                    }
                }
                case ERROR -> statusBar.setMessage("Error: " + event.getError().getMessage());
            }
        });
    }
}

