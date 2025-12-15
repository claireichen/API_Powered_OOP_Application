package org.example.model.strategy;

import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.example.model.strategy.AbstractRecommendationStrategy;
import org.example.service.SpotifyService;

import java.io.IOException;
import java.util.List;

/**
 * Concrete strategy for mood-based recommendations.
 */
public class MoodRecommendationStrategy extends AbstractRecommendationStrategy {

    public MoodRecommendationStrategy(SpotifyService spotifyService) {
        super(spotifyService);
    }

    @Override
    public List<Track> getRecommendations(UserQuery query) throws IOException {
        // Mood has priority; fall back to general text if needed.
        String mood = query != null ? query.getMood() : null;
        String fallback = query != null ? query.getText() : null;
        String finalQuery = buildQuery(mood, fallback);

        UserQuery effective = new UserQuery().setText(finalQuery);
        return spotifyService.searchTracks(effective);
    }
}
