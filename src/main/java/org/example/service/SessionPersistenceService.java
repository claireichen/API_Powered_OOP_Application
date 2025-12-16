package org.example.service;

import org.example.model.AppModel;
import org.example.model.domain.Session;
import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.example.model.repository.JsonSessionRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SessionPersistenceService {

    private final JsonSessionRepository repository;

    public SessionPersistenceService(JsonSessionRepository repository) {
        this.repository = repository;
    }

    /**
     * Build a Session from the current AppModel and save it to the given file.
     */
    public void saveCurrentSession(AppModel model, File file) throws IOException {
        if (file == null) {
            return;
        }

        UserQuery lastQuery = model.getLastQuery();
        List<Track> tracks = model.getCurrentTracks();

        // Use the existing constructor: Session(UserQuery, List<Track>)
        Session session = new Session(lastQuery, tracks);

        repository.save(session, file);
    }


    /**
     * Load a Session from file and apply it to the given AppModel.
     */
    public void loadSessionInto(AppModel model, File file) throws IOException {
        if (file == null) {
            return;
        }

        Session session = repository.load(file);
        if (session == null) {
            return;
        }

        model.setLastQuery(session.getQuery());
        model.setCurrentTracks(session.getTracks());
    }
}
