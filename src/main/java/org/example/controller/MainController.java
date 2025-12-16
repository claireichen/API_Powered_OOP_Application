package org.example.controller;

import org.example.model.*;
import org.example.model.domain.GenerationResult;
import org.example.model.domain.Session;
import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.example.model.strategy.MusicGenerationStrategy;
import org.example.model.strategy.RecommendationStrategy;
import org.example.service.MusicServiceFactory;
import org.example.service.MusicServiceFactory.GenerationMode;
import org.example.service.MusicServiceFactory.RecommendationMode;
import org.example.service.SessionPersistenceService;
import org.example.service.SpotifyService;
import org.example.service.SunoService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController extends MusicEventSource {

    private final AppModel model;
    private final MusicServiceFactory factory;
    private final SpotifyService spotifyService;
    private final SunoService sunoService;
    private final SessionPersistenceService sessionPersistenceService;
    private javax.swing.SwingWorker<?, ?> currentWorker;

    public MainController(AppModel model,
                          MusicServiceFactory factory,
                          SpotifyService spotifyService,
                          SunoService sunoService,
                          SessionPersistenceService sessionPersistenceService) {
        this.model = model;
        this.factory = factory;
        this.spotifyService = spotifyService;
        this.sunoService = sunoService;
        this.sessionPersistenceService = sessionPersistenceService;
    }

    public void requestRecommendations(UserQuery query, RecommendationMode mode) {
        RecommendationStrategy strategy = factory.createRecommendationStrategy(mode);
        fireEvent(MusicEvent.of(EventType.RECOMMENDATION_STARTED, null));

        currentWorker = new SwingWorker<List<Track>, Void>() {
            @Override
            protected List<Track> doInBackground() throws Exception {
                return strategy.getRecommendations(query);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    fireEvent(MusicEvent.of(EventType.RECOMMENDATION_CANCELLED, null));
                    return;
                }
                try {
                    List<Track> tracks = get();
                    model.setCurrentTracks(tracks);
                    model.setLastQuery(query);
                    fireEvent(MusicEvent.of(EventType.RECOMMENDATION_COMPLETED, tracks));
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };
        currentWorker.execute();
    }

    public void requestGeneration(UserQuery query, GenerationMode mode) {
        MusicGenerationStrategy strategy = factory.createGenerationStrategy(mode);
        fireEvent(MusicEvent.of(EventType.GENERATION_STARTED, null));

        currentWorker = new SwingWorker<GenerationResult, Void>() {
            @Override
            protected GenerationResult doInBackground() throws Exception {
                Thread.sleep(10_000);
                if (isCancelled()) {
                    return null;
                }
                return strategy.generate(query);
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        return;
                    }

                    GenerationResult result = get();
                    if (result != null) {
                        model.setLastGenerationResult(result);
                        fireEvent(MusicEvent.of(EventType.GENERATION_COMPLETED, result));
                    } else {
                        fireEvent(MusicEvent.error(new IllegalStateException("Generation cancelled or no result")));
                    }
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };

        currentWorker.execute();
    }

    public void saveCurrentSession(File file) {
        if (file == null) {
            return;
        }

        // Ensure we actually have something to save
        if (model.getLastQuery() == null ||
                model.getCurrentTracks() == null ||
                model.getCurrentTracks().isEmpty()) {
            fireEvent(MusicEvent.error(
                    new IllegalStateException("Nothing to save: perform a search first.")));
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Delegate to the persistence service
                sessionPersistenceService.saveCurrentSession(model, file);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // rethrow exceptions if any
                    fireEvent(MusicEvent.of(EventType.SESSION_SAVED, file));
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };

        worker.execute();
    }

    public void loadSession(File file) {
        if (file == null) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Load into the existing model
                sessionPersistenceService.loadSessionInto(model, file);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // rethrow exceptions if any

                    // After load, model has new query + tracks
                    fireEvent(MusicEvent.of(EventType.SESSION_LOADED, null));
                    // Also fire RECOMMENDATION_COMPLETED so the table refreshes
                    fireEvent(MusicEvent.of(EventType.RECOMMENDATION_COMPLETED,
                            model.getCurrentTracks()));
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };

        worker.execute();
    }

    // Called by MainFrame with the combo-box label
    public void requestRecommendationsFromUI(UserQuery query, String modeLabel) {
        MusicServiceFactory.RecommendationMode mode;

        String normalized = modeLabel == null ? "" : modeLabel.toLowerCase();
        switch (normalized) {
            case "genre" -> mode = MusicServiceFactory.RecommendationMode.GENRE;
            case "artist" -> mode = MusicServiceFactory.RecommendationMode.ARTIST;
            case "mood"  -> mode = MusicServiceFactory.RecommendationMode.MOOD;
            default -> mode = MusicServiceFactory.RecommendationMode.MOOD;
        }

        requestRecommendations(query, mode);
    }

    public void requestGenerationFromUI(UserQuery query) {
        // For now we only support instrumental mode via Suno
        requestGeneration(query, MusicServiceFactory.GenerationMode.INSTRUMENTAL);
    }

    /** Cancel the currently running SwingWorker, if any. */
    public void cancelCurrentOperation() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);

            fireEvent(MusicEvent.of(EventType.GENERATION_CANCELLED, null));
        }
    }

    /** Open the most recently generated audio URL in the browser. */
    public void openLastGeneratedAudio() {
        GenerationResult result = model.getLastGenerationResult();
        if (result == null || result.getAudioUrl() == null) {
            return;
        }
        try {
            java.awt.Desktop.getDesktop()
                    .browse(java.net.URI.create(result.getAudioUrl()));
        } catch (Exception e) {
            fireEvent(MusicEvent.error(e));
        }
    }
}
