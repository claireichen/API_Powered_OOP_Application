package org.example.service;

import org.example.model.APIClient;
import org.example.model.domain.Track;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyServiceParsingTest {

    @Test
    void parseTracksFromSearchReturnsEmptyListWhenNoTracksKey() {
        SpotifyService service = new SpotifyService(APIClient.getInstance());

        String json = "{}";

        List<Track> tracks = service.parseTracksFromSearch(json);

        assertNotNull(tracks);
        assertTrue(tracks.isEmpty());
    }

    @Test
    void parseTracksFromSearchParsesBasicTrackFields() {
        SpotifyService service = new SpotifyService(APIClient.getInstance());

        String json = """
            {
              "tracks": {
                "items": [
                  {
                    "id": "track123",
                    "name": "Lo-Fi Test",
                    "preview_url": "https://example.com/preview.mp3",
                    "album": { "name": "Test Album" },
                    "artists": [ { "name": "Test Artist" } ]
                  }
                ]
              }
            }
            """;

        List<Track> tracks = service.parseTracksFromSearch(json);

        assertEquals(1, tracks.size());
        Track t = tracks.get(0);
        assertEquals("track123", t.getId());
        assertEquals("Lo-Fi Test", t.getName());
        assertEquals("Test Artist", t.getArtist());
        assertEquals("Test Album", t.getAlbum());
        assertEquals("https://example.com/preview.mp3", t.getPreviewUrl());
    }
}
