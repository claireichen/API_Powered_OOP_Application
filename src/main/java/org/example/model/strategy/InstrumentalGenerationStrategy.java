package org.example.model.strategy;

import org.example.model.domain.GenerationResult;
import org.example.model.domain.UserQuery;
import org.example.service.SunoService;

import java.io.IOException;
import java.util.Objects;

/**
 * Concrete strategy that delegates to Suno/MusicAPI to generate
 * instrumental music.
 */
public class InstrumentalGenerationStrategy implements MusicGenerationStrategy {

    private final SunoService sunoService;

    public InstrumentalGenerationStrategy(SunoService sunoService) {
        this.sunoService = Objects.requireNonNull(sunoService, "sunoService");
    }

    @Override
    public GenerationResult generate(UserQuery query) throws IOException {
        // Two-step flow: create + poll
        GenerationResult initial = sunoService.requestInstrumental(query);

        if (initial != null && initial.getAudioUrl() != null) {
            // Sometimes the API returns an audio URL immediately
            return initial;
        }
        if (initial == null || initial.getTaskId() == null) {
            return new GenerationResult()
                    .setStatus("error")
                    .setAudioUrl(null);
        }
        return sunoService.pollGenerationStatus(initial.getTaskId());
    }

    @Override
    public String toString() {
        return "InstrumentalGenerationStrategy";
    }
}
