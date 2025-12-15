package org.example.model.strategy;

import org.example.model.domain.GenerationResult;
import org.example.model.domain.UserQuery;

import java.io.IOException;

/**
 * Abstraction for music generation behavior.
 */
public interface MusicGenerationStrategy {

    GenerationResult generate(UserQuery query) throws IOException;
}
