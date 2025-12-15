package org.example.model.strategy;

import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.example.service.SpotifyService;

import java.io.IOException;
import java.util.List;

/**
 * Concrete strategy for genre-based recommendations.
 */
public class GenreRecommendationStrategy extends AbstractRecommendationStrategy {

    public GenreRecommendationStrategy(SpotifyService spotifyService) {
        super(spotifyService);
    }

    @Override
    public List<Track> getRecommendations(UserQuery query) throws IOException {
        String genre = query != null ? query.getGenre() : null;
        String fallback = query != null ? query.getText() : null;
        String finalQuery = buildQuery(genre, fallback);

        UserQuery effective = new UserQuery().setText(finalQuery);
        return spotifyService.searchTracks(effective);
    }
}
