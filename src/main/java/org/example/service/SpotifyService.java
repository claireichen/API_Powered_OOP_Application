package org.example.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.model.APIClient;
import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpotifyService {

    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";

    private final APIClient apiClient;

    public SpotifyService(APIClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * First real API call: use Spotify Search API to find tracks for the query text.
     */
    public List<Track> searchTracks(UserQuery query) throws IOException {
        if (query == null || query.getText() == null || query.getText().isBlank()) {
            return Collections.emptyList();
        }

        try {
            String accessToken = apiClient.getOrRefreshSpotifyToken();
            String encoded = APIClient.urlEncode(query.getText());
            String url = SPOTIFY_API_BASE +
                    "/search?offset=0&limit=10&query=" + encoded + "&type=track";

            HttpResponse<String> response =
                    apiClient.getWithRetry(url, "Bearer " + accessToken);

            int status = response.statusCode();
            String body = response.body();

            if (status != 200) {
                throw new IOException("Spotify search failed: HTTP " + status + " – " + body);
            }

            // Debug: see what JSON we get (you can comment this out later)
            System.out.println("DEBUG raw JSON (first 400 chars):");
            System.out.println(body.substring(0, Math.min(400, body.length())));

            return parseTracksFromSearch(body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while calling Spotify", e);
        }
    }

    // -------------------------------------------------------------------------
    // Simple "recommendation" wrappers for your strategies
    // -------------------------------------------------------------------------

    public List<Track> getRecommendationsForMood(UserQuery query) throws IOException {
        if (query.getMood() != null && !query.getMood().isBlank()) {
            return searchTracks(new UserQuery().setText(query.getMood()));
        }
        return searchTracks(query);
    }

    public List<Track> getRecommendationsForGenre(UserQuery query) throws IOException {
        if (query.getGenre() != null && !query.getGenre().isBlank()) {
            return searchTracks(new UserQuery().setText(query.getGenre()));
        }
        return searchTracks(query);
    }

    public List<Track> getRecommendationsForArtist(UserQuery query) throws IOException {
        if (query.getArtist() != null && !query.getArtist().isBlank()) {
            return searchTracks(new UserQuery().setText(query.getArtist()));
        }
        return searchTracks(query);
    }

    // -------------------------------------------------------------------------
    // JSON parsing using Gson
    // -------------------------------------------------------------------------

    List<Track> parseTracksFromSearch(String json) {
        List<Track> tracks = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!root.has("tracks") || root.get("tracks").isJsonNull()) {
            System.out.println("DEBUG: no 'tracks' object in JSON");
            return tracks;
        }

        JsonObject tracksObj = root.getAsJsonObject("tracks");
        if (!tracksObj.has("items") || tracksObj.get("items").isJsonNull()) {
            System.out.println("DEBUG: no 'items' array in tracks");
            return tracks;
        }

        JsonArray items = tracksObj.getAsJsonArray("items");
        for (JsonElement elem : items) {
            if (!elem.isJsonObject()) continue;
            JsonObject item = elem.getAsJsonObject();

            String id = optString(item, "id");
            String name = optString(item, "name");
            String previewUrl = optString(item, "preview_url");

            // Album name
            String albumName = "";
            if (item.has("album") && item.get("album").isJsonObject()) {
                JsonObject album = item.getAsJsonObject("album");
                albumName = optString(album, "name");
            }

            // Artist name (first artist in array)
            String artistName = "";
            if (item.has("artists") && item.get("artists").isJsonArray()) {
                JsonArray artists = item.getAsJsonArray("artists");
                if (!artists.isJsonNull() && artists.size() > 0 && artists.get(0).isJsonObject()) {
                    JsonObject artistObj = artists.get(0).getAsJsonObject();
                    artistName = optString(artistObj, "name");
                }
            }

            if (name != null && artistName != null) {
                Track t = new Track()
                        .setId(id)
                        .setName(name)
                        .setArtist(artistName)
                        .setAlbum(albumName != null ? albumName : "")
                        .setPreviewUrl(previewUrl);
                tracks.add(t);
            }
        }

        System.out.println("DEBUG: parsed " + tracks.size() + " track(s) from JSON.");
        return tracks;
    }

    private String optString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsString();
    }
}
