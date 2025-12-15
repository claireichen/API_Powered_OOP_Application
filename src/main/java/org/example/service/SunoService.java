package org.example.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.model.APIClient;
import org.example.model.domain.GenerationResult;
import org.example.model.domain.UserQuery;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Objects;

public class SunoService {

    private static final int POLL_INTERVAL_MS = 2000;   // 2s
    private static final int MAX_POLL_ATTEMPTS = 20;    // ~40s total

    private final APIClient apiClient;
    private final String baseUrl;   // e.g. https://api.musicapi.ai
    private final String apiKey;    // your Bearer token

    public SunoService(APIClient apiClient) {
        this.apiClient = apiClient;
        this.baseUrl = Objects.requireNonNull(apiClient.getSunoBaseUrl(),
                "suno.baseUrl not set in config.properties");
        this.apiKey = Objects.requireNonNull(apiClient.getSunoApiKey(),
                "suno.apiKey not set in config.properties");
    }

    /**
     * Start an instrumental track generation using MusicAPI (Sonic model).
     * This calls POST /api/v1/sonic/create and returns a GenerationResult
     * containing the task_id (used for polling).
     */
    public GenerationResult requestInstrumental(UserQuery query) throws IOException {
        String url = baseUrl + "/api/v1/sonic/create";

        String prompt = buildPrompt(query); // lyrics / description
        String title = (query.getText() != null && !query.getText().isBlank())
                ? query.getText()
                : "MuseMix Instrumental";
        String tags = buildTags(query);

        JsonObject body = new JsonObject();
        body.addProperty("custom_mode", true);
        body.addProperty("prompt", prompt);
        body.addProperty("title", title);
        body.addProperty("tags", tags);
        body.addProperty("make_instrumental", true);      // instrumental only
        body.addProperty("mv", "sonic-v5");               // model version
        // you can also add style_weight / weirdness_constraint later

        String jsonBody = body.toString();

        try {
            HttpResponse<String> response = apiClient.postJson(url, jsonBody, apiKey);
            if (response.statusCode() != 200) {
                throw new IOException("MusicAPI create failed: HTTP "
                        + response.statusCode() + " - " + response.body());
            }

            return parseGenerationResultCreate(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while calling MusicAPI", e);
        }
    }

    /**
     * Poll the status of a task until it finishes or we time out.
     * Uses GET /api/v1/sonic/task/{task_id}.
     */
    public GenerationResult pollGenerationStatus(String taskId) throws IOException {
        String url = baseUrl + "/api/v1/sonic/task/" + taskId;

        GenerationResult lastResult = new GenerationResult()
                .setTaskId(taskId)
                .setStatus("pending");

        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = apiClient.get(url, apiKey);
                int statusCode = response.statusCode();

                if (statusCode == 202) {
                    // task not ready yet, just wait and poll again
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                if (statusCode != 200) {
                    throw new IOException("MusicAPI poll failed: HTTP "
                            + statusCode + " – " + response.body());
                }

                lastResult = parseGenerationResultPoll(taskId, response.body());
                String status = lastResult.getStatus() != null ? lastResult.getStatus() : "";
                String lower = status.toLowerCase();

                // If we have an audio URL, we're done no matter what the text says
                if (lastResult.getAudioUrl() != null && !lastResult.getAudioUrl().isBlank()) {
                    return lastResult;
                }

                if (lower.contains("succeeded") || lower.contains("success")) {
                    return lastResult;
                }
                if (lower.contains("failed") || lower.contains("error")) {
                    return lastResult;
                }

                // still processing
                Thread.sleep(POLL_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while polling MusicAPI", e);
            }
        }

        // Only call it timeout if we never got an audio URL or a terminal status
        if (lastResult.getAudioUrl() == null || lastResult.getAudioUrl().isBlank()) {
            lastResult.setStatus("timeout");
        }
        return lastResult;
    }

    // ---------- JSON parsing helpers ----------

    // Parse the POST /sonic/create response: { "message": "success", "task_id": "..." }
    private GenerationResult parseGenerationResultCreate(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        String taskId = null;
        if (root.has("task_id") && !root.get("task_id").isJsonNull()) {
            taskId = root.get("task_id").getAsString();
        }

        String message = null;
        if (root.has("message") && !root.get("message").isJsonNull()) {
            message = root.get("message").getAsString();
        }

        return new GenerationResult()
                .setTaskId(taskId)
                .setStatus(message != null ? message : "submitted")
                .setAudioUrl(null);
    }

    // Parse the GET /sonic/task/{task_id} response:
    // { code, message, data: [ { state, audio_url, ... }, ... ] }
    private GenerationResult parseGenerationResultPoll(String taskId, String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        String message = null;
        if (root.has("message") && !root.get("message").isJsonNull()) {
            message = root.get("message").getAsString();
        }

        String state = message;
        String audioUrl = null;

        if (root.has("data") && root.get("data").isJsonArray()) {
            JsonArray arr = root.getAsJsonArray("data");
            if (arr.size() > 0 && arr.get(0).isJsonObject()) {
                JsonObject item = arr.get(0).getAsJsonObject();

                if (item.has("state") && !item.get("state").isJsonNull()) {
                    state = item.get("state").getAsString();
                }
                if (item.has("audio_url") && !item.get("audio_url").isJsonNull()) {
                    audioUrl = item.get("audio_url").getAsString();
                }
            }
        }

        return new GenerationResult()
                .setTaskId(taskId)
                .setStatus(state)
                .setAudioUrl(audioUrl);
    }

    // ---------- Prompt & tags helpers ----------

    private String buildPrompt(UserQuery query) {
        StringBuilder sb = new StringBuilder();
        if (query.getText() != null && !query.getText().isBlank()) {
            sb.append(query.getText());
        }
        if (query.getMood() != null && !query.getMood().isBlank()) {
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append("Mood: ").append(query.getMood());
        }
        if (query.getGenre() != null && !query.getGenre().isBlank()) {
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append("Genre: ").append(query.getGenre());
        }
        if (sb.isEmpty()) {
            sb.append("Chill instrumental background music");
        }
        return sb.toString();
    }

    private String buildTags(UserQuery query) {
        StringBuilder sb = new StringBuilder();
        if (query.getGenre() != null && !query.getGenre().isBlank()) {
            sb.append(query.getGenre());
        }
        if (query.getMood() != null && !query.getMood().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(query.getMood());
        }
        if (sb.isEmpty()) {
            sb.append("instrumental");
        }
        return sb.toString();
    }
}
