package org.example.model;

import org.example.model.domain.GenerationResult;
import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Central application state (the "M" in MVC).
 * Demonstrates encapsulation by hiding internal lists
 * and exposing only safe, validated accessors.
 */
public class AppModel {

    private final List<Track> currentTracks = new ArrayList<>();
    private UserQuery lastQuery;
    private GenerationResult lastGenerationResult;

    public AppModel() {
    }

    // --- Current tracks ---

    /**
     * Replace the current tracks with a copy of the given list.
     * The internal list cannot be modified directly from outside.
     */
    public void setCurrentTracks(List<Track> tracks) {
        currentTracks.clear();
        if (tracks != null) {
            currentTracks.addAll(tracks);
        }
    }

    /**
     * Returns an unmodifiable view to preserve encapsulation.
     */
    public List<Track> getCurrentTracks() {
        return Collections.unmodifiableList(currentTracks);
    }

    // --- Last query ---

    public void setLastQuery(UserQuery lastQuery) {
        this.lastQuery = lastQuery; // nullable is OK
    }

    public UserQuery getLastQuery() {
        return lastQuery;
    }

    // --- Last generation result ---

    public void setLastGenerationResult(GenerationResult lastGenerationResult) {
        this.lastGenerationResult = lastGenerationResult;
    }

    public GenerationResult getLastGenerationResult() {
        return lastGenerationResult;
    }

    @Override
    public String toString() {
        return "AppModel{" +
                "currentTracks=" + currentTracks.size() +
                ", lastQuery=" + (lastQuery != null ? lastQuery.getText() : "null") +
                ", lastGenerationResultStatus=" +
                (lastGenerationResult != null ? lastGenerationResult.getStatus() : "null") +
                '}';
    }
}
