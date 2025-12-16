package org.example.model.strategy;

import org.example.model.APIClient;
import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.example.service.SpotifyService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoodRecommendationStrategyTest {

    /**
     * Fake SpotifyService used only for this test.
     * It NEVER hits the real network: it just records the query
     * and returns a single fake track.
     */
    static class FakeSpotifyService extends SpotifyService {

        UserQuery lastQueryPassed;

        FakeSpotifyService() {
            // We still need an APIClient for the superclass constructor,
            // but we won't use any real HTTP methods.
            super(APIClient.getInstance());
        }

        @Override
        public List<Track> getRecommendationsForMood(UserQuery query) {
            lastQueryPassed = query;
            return List.of(new Track().setName("Fake"));
        }

        // Safety: in case the strategy calls searchTracks directly instead
        @Override
        public List<Track> searchTracks(UserQuery query) {
            lastQueryPassed = query;
            return List.of(new Track().setName("Fake"));
        }
    }

    @Test
    void usesMoodWhenPresentOtherwiseFallsBackToText() throws IOException {
        FakeSpotifyService spotify = new FakeSpotifyService();
        MoodRecommendationStrategy strategy = new MoodRecommendationStrategy(spotify);

        UserQuery q = new UserQuery()
                .setText("lofi chill")
                .setMood("sleepy");

        List<Track> tracks = strategy.getRecommendations(q);

        // We get exactly one fake track back
        assertEquals(1, tracks.size());
        assertEquals("Fake", tracks.get(0).getName());

        // And the strategy passed the MOOD ("sleepy") as the query text
        assertNotNull(spotify.lastQueryPassed);
        assertEquals("sleepy", spotify.lastQueryPassed.getText());
    }
}
