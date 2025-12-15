package org.example.controller;

import org.example.model.domain.GenerationResult;
import org.example.model.domain.Track;

import java.util.Collections;
import java.util.List;

public final class MusicEvent {

    private final EventType type;
    private final Object payload;

    private MusicEvent(EventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public static MusicEvent of(EventType type, Object payload) {
        return new MusicEvent(type, payload);
    }

    public static MusicEvent error(Throwable error) {
        return new MusicEvent(EventType.ERROR, error);
    }

    // --- Methods used by MainFrame ---

    /** Event type (RECOMMENDATION_STARTED, COMPLETED, ERROR, etc.) */
    public EventType type() {
        return type;
    }

    /** Payload interpreted as a list of tracks (for RECOMMENDATION_COMPLETED). */
    @SuppressWarnings("unchecked")
    public List<Track> tracksPayload() {
        if (payload instanceof List<?> list) {
            return (List<Track>) list;
        }
        return Collections.emptyList();
    }

    /** Status string, primarily for generation results. */
    public String status() {
        if (payload instanceof GenerationResult gr) {
            return gr.getStatus();
        }
        if (payload instanceof String s) {
            return s;
        }
        return null;
    }

    /** Human-readable error message, if this is an error event. */
    public String errorMessage() {
        if (payload instanceof Throwable t) {
            return t.getMessage();
        }
        if (payload instanceof String s) {
            return s;
        }
        return "Unknown error";
    }

    /** Raw payload if needed elsewhere. */
    public Object payload() {
        return payload;
    }
}
