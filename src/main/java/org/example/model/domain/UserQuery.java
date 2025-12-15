package org.example.model.domain;

/**
 * Encapsulates the user's query parameters.
 * Demonstrates encapsulation with validation/normalization in setters.
 */
public class UserQuery {

    private String text;
    private String mood;
    private String genre;
    private String artist;

    public String getText() {
        return text;
    }

    public UserQuery setText(String text) {
        this.text = normalize(text);
        return this;
    }

    public String getMood() {
        return mood;
    }

    public UserQuery setMood(String mood) {
        this.mood = normalize(mood);
        return this;
    }

    public String getGenre() {
        return genre;
    }

    public UserQuery setGenre(String genre) {
        this.genre = normalize(genre);
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public UserQuery setArtist(String artist) {
        this.artist = normalize(artist);
        return this;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String toString() {
        return "UserQuery{" +
                "text='" + text + '\'' +
                ", mood='" + mood + '\'' +
                ", genre='" + genre + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}
