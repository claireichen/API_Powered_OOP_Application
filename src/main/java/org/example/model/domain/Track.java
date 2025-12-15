package org.example.model.domain;

import java.util.Objects;

/**
 * Domain model for a Spotify track.
 * Encapsulation: all fields private, accessed via getters/setters.
 * Polymorphism: overrides equals/hashCode/toString.
 */
public class Track {

    private String id;
    private String name;
    private String artist;
    private String album;
    private String previewUrl;

    public String getId() {
        return id;
    }

    public Track setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Track setName(String name) {
        this.name = safeTrim(name);
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public Track setArtist(String artist) {
        this.artist = safeTrim(artist);
        return this;
    }

    public String getAlbum() {
        return album;
    }

    public Track setAlbum(String album) {
        this.album = safeTrim(album);
        return this;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public Track setPreviewUrl(String previewUrl) {
        this.previewUrl = safeTrim(previewUrl);
        return this;
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    // Polymorphism: meaningful equality based on Spotify track id.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track track)) return false;
        return Objects.equals(id, track.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Useful for debugging / UI
    @Override
    public String toString() {
        return name + " — " + artist + " (" + album + ")";
    }
}
