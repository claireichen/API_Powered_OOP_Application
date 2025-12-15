package org.example.view;

import org.example.controller.EventType;
import org.example.controller.MainController;
import org.example.controller.MusicEvent;
import org.example.controller.MusicEventListener;
import org.example.model.AppModel;
import org.example.model.domain.UserQuery;
import org.example.view.panels.GenerationPanel;
import org.example.view.panels.ResultPanel;
import org.example.view.panels.SearchPanel;
import org.example.view.panels.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame implements MusicEventListener {

    private final MainController controller;
    private final SearchPanel searchPanel;
    private final ResultPanel resultPanel;
    private final GenerationPanel generationPanel;
    private final StatusBar statusBar;

    public MainFrame(MainController controller) {
        super("MuseMix – Music Recommender & Generator");
        this.controller = controller;
        this.controller.addListener(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tabs
        JTabbedPane tabs = new JTabbedPane();

        // Recommendations tab (search + results side by side)
        searchPanel = new SearchPanel();
        resultPanel = new ResultPanel();

        JSplitPane recSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                searchPanel,
                new JScrollPane(resultPanel)
        );
        recSplit.setResizeWeight(0.35);

        tabs.addTab("Recommendations", recSplit);

        // Generation tab
        generationPanel = new GenerationPanel();
        tabs.addTab("Generation", generationPanel);

        add(tabs, BorderLayout.CENTER);

        // Status bar
        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        wireActions();

        setSize(960, 600);
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Save Session...");
        JMenuItem load = new JMenuItem("Load Session...");
        JMenuItem exit = new JMenuItem("Exit");

        save.addActionListener(e -> doSaveSession());
        load.addActionListener(e -> doLoadSession());
        exit.addActionListener(e -> dispose());

        file.add(save);
        file.add(load);
        file.addSeparator();
        file.add(exit);

        bar.add(file);

        return bar;
    }

    private void wireActions() {
        // Recommendations: Search
        searchPanel.onSearch(e -> {
            UserQuery query = new UserQuery()
                    .setText(searchPanel.getQueryText());
            // Determine mode based on combo
            String mode = searchPanel.getSelectedMode();
            controller.requestRecommendationsFromUI(query, mode);
        });

        // Recommendations: Cancel
        searchPanel.onCancel(e -> controller.cancelCurrentOperation());

        // Generation: Generate
        generationPanel.onGenerate(e -> {
            UserQuery query = new UserQuery()
                    .setText(generationPanel.getPrompt())
                    .setGenre(generationPanel.getGenre())
                    .setMood(generationPanel.getMood());
            controller.requestGenerationFromUI(query);
        });

        // Generation: Cancel
        generationPanel.onCancel(e -> controller.cancelCurrentOperation());

        // Generation: open audio link (controller should know last URL)
        generationPanel.onOpenLink(e -> controller.openLastGeneratedAudio());
    }

    private void doSaveSession() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            controller.saveCurrentSession(file);
        }
    }

    private void doLoadSession() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            controller.loadSession(file);
        }
    }

    // --- MusicEventListener implementation ---

    @Override
    public void onMusicEvent(MusicEvent event) {
        EventType type = event.type();

        switch (type) {
            case RECOMMENDATION_STARTED -> {
                statusBar.setMessage("Fetching recommendations...");
                statusBar.setBusy(true);
                searchPanel.setBusy(true);
            }
            case RECOMMENDATION_COMPLETED -> {
                statusBar.setMessage("Recommendations loaded.");
                statusBar.setBusy(false);
                searchPanel.setBusy(false);
                // payload assumed List<Track>, ResultPanel already knows how to display
                resultPanel.setTracks(event.tracksPayload());
            }
            case GENERATION_STARTED -> {
                statusBar.setMessage("Generating music...");
                statusBar.setBusy(true);
                generationPanel.setBusy(true);
                generationPanel.setStatusText("pending");
            }
            case GENERATION_COMPLETED -> {
                statusBar.setMessage("Generation complete.");
                statusBar.setBusy(false);
                generationPanel.setBusy(false);
                generationPanel.setStatusText(event.status());
                generationPanel.setLinkText("Click here to open audio");
            }
            case SESSION_SAVED -> statusBar.setMessage("Session saved.");
            case SESSION_LOADED -> statusBar.setMessage("Session loaded.");
            case ERROR -> {
                statusBar.setBusy(false);
                searchPanel.setBusy(false);
                generationPanel.setBusy(false);
                statusBar.setMessage("Error: " + event.errorMessage());
                JOptionPane.showMessageDialog(
                        this,
                        event.errorMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            default -> {
            }
        }
    }
}
