package org.example.controller;

public class MusicEvent {

    private final EventType type;
    private final Object payload;
    private final Throwable error;

    public MusicEvent(EventType type, Object payload, Throwable error) {
        this.type = type;
        this.payload = payload;
        this.error = error;
    }

    public static MusicEvent of(EventType type, Object payload) {
        return new MusicEvent(type, payload, null);
    }

    public static MusicEvent error(Throwable error) {
        return new MusicEvent(EventType.ERROR, null, error);
    }

    public EventType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public Throwable getError() {
        return error;
    }
}