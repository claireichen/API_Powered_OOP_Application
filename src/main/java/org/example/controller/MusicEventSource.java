package org.example.controller;

import java.util.ArrayList;
import java.util.List;

public class MusicEventSource {

    private final List<MusicEventListener> listeners = new ArrayList<>();

    public void addListener(MusicEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MusicEventListener listener) {
        listeners.remove(listener);
    }

    protected void fireEvent(MusicEvent event) {
        for (MusicEventListener listener : new ArrayList<>(listeners)) {
            listener.onMusicEvent(event);
        }
    }
}