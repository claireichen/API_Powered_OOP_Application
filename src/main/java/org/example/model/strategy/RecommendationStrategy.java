package org.example.model.strategy;

import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;

import java.io.IOException;
import java.util.List;

/**
 * Abstraction for how recommendations are generated.
 * Different strategies (mood/genre/artist) implement this interface.
 */
public interface RecommendationStrategy {

    List<Track> getRecommendations(UserQuery query) throws IOException;
}
