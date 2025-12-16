package org.example.service;

import org.example.model.APIClient;
import org.example.model.strategy.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicServiceFactoryTest {

    private MusicServiceFactory createFactory() {
        APIClient client = APIClient.getInstance();
        SpotifyService spotify = new SpotifyService(client);
        SunoService suno = new SunoService(client);
        return new MusicServiceFactory(spotify, suno);
    }

    @Test
    void createsGenreRecommendationStrategyForGenreMode() {
        MusicServiceFactory factory = createFactory();

        RecommendationStrategy strategy =
                factory.createRecommendationStrategy(MusicServiceFactory.RecommendationMode.GENRE);

        assertNotNull(strategy);
        assertTrue(strategy instanceof GenreRecommendationStrategy);
    }

    @Test
    void createsMoodRecommendationStrategyForMoodMode() {
        MusicServiceFactory factory = createFactory();

        RecommendationStrategy strategy =
                factory.createRecommendationStrategy(MusicServiceFactory.RecommendationMode.MOOD);

        assertNotNull(strategy);
        assertTrue(strategy instanceof MoodRecommendationStrategy);
    }

    @Test
    void createsArtistSeedRecommendationStrategyForArtistMode() {
        MusicServiceFactory factory = createFactory();

        RecommendationStrategy strategy =
                factory.createRecommendationStrategy(MusicServiceFactory.RecommendationMode.ARTIST);

        assertNotNull(strategy);
        assertTrue(strategy instanceof ArtistSeedRecommendationStrategy);
    }

    @Test
    void createsInstrumentalGenerationStrategyForInstrumentalMode() {
        MusicServiceFactory factory = createFactory();

        MusicGenerationStrategy strategy =
                factory.createGenerationStrategy(MusicServiceFactory.GenerationMode.INSTRUMENTAL);

        assertNotNull(strategy);
        assertTrue(strategy instanceof InstrumentalGenerationStrategy);
    }
}
