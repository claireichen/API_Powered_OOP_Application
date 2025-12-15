package org.example.model.strategy;

import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.example.service.SpotifyService;

import java.io.IOException;
import java.util.List;

/**
 * Concrete strategy for artist-based recommendations.
 */
public class ArtistSeedRecommendationStrategy extends AbstractRecommendationStrategy {

    public ArtistSeedRecommendationStrategy(SpotifyService spotifyService) {
        super(spotifyService);
    }

    @Override
    public List<Track> getRecommendations(UserQuery query) throws IOException {
        String artist = query != null ? query.getArtist() : null;
        String fallback = query != null ? query.getText() : null;
        String finalQuery = buildQuery(artist, fallback);

        UserQuery effective = new UserQuery().setText(finalQuery);
        return spotifyService.searchTracks(effective);
    }
}
