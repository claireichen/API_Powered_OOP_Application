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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController extends MusicEventSource {

    private final AppModel model;
    private final MusicServiceFactory factory;
    private final SessionPersistenceService sessionService;

    public MainController(AppModel model, MusicServiceFactory factory, SessionPersistenceService sessionService) {
        this.model = model;
        this.factory = factory;
        this.sessionService = sessionService;
    }

    public void requestRecommendations(UserQuery query, RecommendationMode mode) {
        RecommendationStrategy strategy = factory.createRecommendationStrategy(mode);
        fireEvent(MusicEvent.of(EventType.RECOMMENDATION_STARTED, null));

        SwingWorker<List<Track>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Track> doInBackground() throws Exception {
                return strategy.getRecommendations(query);
            }

            @Override
            protected void done() {
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
        worker.execute();
    }

    public void requestGeneration(UserQuery query, GenerationMode mode) {
        MusicGenerationStrategy strategy = factory.createGenerationStrategy(mode);
        fireEvent(MusicEvent.of(EventType.GENERATION_STARTED, null));

        SwingWorker<GenerationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected GenerationResult doInBackground() throws Exception {
                return strategy.generate(query);
            }

            @Override
            protected void done() {
                try {
                    GenerationResult result = get();
                    model.setLastGenerationResult(result);
                    fireEvent(MusicEvent.of(EventType.GENERATION_COMPLETED, result));
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };
        worker.execute();
    }

    public void saveCurrentSession(File file) {
        Session session = new Session(model.getLastQuery(), model.getCurrentTracks());

        if (session.getQuery() == null || session.getTracks().isEmpty()) {
            fireEvent(MusicEvent.error(
                    new IllegalStateException("Nothing to save: perform a search first.")));
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sessionService.save(session, file);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // to rethrow exceptions if any
                    fireEvent(MusicEvent.of(EventType.SESSION_SAVED, file));
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };
        worker.execute();
    }

    public void loadSession(File file) {
        SwingWorker<Session, Void> worker = new SwingWorker<>() {
            @Override
            protected Session doInBackground() throws Exception {
                return sessionService.load(file);
            }

            @Override
            protected void done() {
                try {
                    Session session = get();
                    if (session != null) {
                        model.setCurrentTracks(session.getTracks());
                        model.setLastQuery(session.getQuery());
                        fireEvent(MusicEvent.of(EventType.SESSION_LOADED, session));
                    } else {
                        fireEvent(MusicEvent.error(
                                new IOException("Loaded session was null")));
                    }
                } catch (Exception e) {
                    fireEvent(MusicEvent.error(e));
                }
            }
        };
        worker.execute();
    }
}
