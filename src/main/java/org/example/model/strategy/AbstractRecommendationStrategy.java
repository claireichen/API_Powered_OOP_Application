package org.example.model.strategy;

import org.example.service.SpotifyService;

import java.util.Objects;

/**
 * Abstract base class for recommendation strategies.
 * Demonstrates inheritance: concrete strategies extend this base.
 */
public abstract class AbstractRecommendationStrategy implements RecommendationStrategy {

    protected final SpotifyService spotifyService;

    protected AbstractRecommendationStrategy(SpotifyService spotifyService) {
        this.spotifyService = Objects.requireNonNull(spotifyService, "spotifyService");
    }

    /**
     * Helper to build a query string from the given field,
     * falling back to a default if necessary.
     */
    protected String buildQuery(String fieldValue, String fallback) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return fallback;
        }
        return fieldValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
