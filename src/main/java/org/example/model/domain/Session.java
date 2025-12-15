package org.example.model.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable-ish snapshot of a search session.
 * Encapsulation: internal list is copied and exposed as unmodifiable.
 */
public class Session {

    private final UserQuery query;
    private final List<Track> tracks;

    public Session(UserQuery query, List<Track> tracks) {
        this.query = query;
        this.tracks = new ArrayList<>();
        if (tracks != null) {
            this.tracks.addAll(tracks);
        }
    }

    public UserQuery getQuery() {
        return query;
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    @Override
    public String toString() {
        return "Session{" +
                "query=" + query +
                ", tracks=" + tracks.size() +
                '}';
    }
}
