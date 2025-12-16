package org.example.model.repository;

import org.example.model.domain.Session;
import org.example.model.domain.Track;
import org.example.model.domain.UserQuery;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonSessionRepositoryTest {

    @Test
    void saveAndLoadRoundTrip() throws Exception {
        File tmp = File.createTempFile("musemix-session", ".json");
        tmp.deleteOnExit();

        JsonSessionRepository repo = new JsonSessionRepository();

        UserQuery query = new UserQuery().setText("lofi chill");
        Track track = new Track()
                .setId("id1")
                .setName("Track 1")
                .setArtist("Artist")
                .setAlbum("Album");

        // 👇 use the real constructor of Session
        Session original = new Session(query, List.of(track));

        repo.save(original, tmp);
        Session loaded = repo.load(tmp);

        assertNotNull(loaded);
        assertNotNull(loaded.getQuery());
        assertEquals("lofi chill", loaded.getQuery().getText());

        assertNotNull(loaded.getTracks());
        assertEquals(1, loaded.getTracks().size());

        Track loadedTrack = loaded.getTracks().get(0);
        assertEquals("Track 1", loadedTrack.getName());
        assertEquals("Artist", loadedTrack.getArtist());
        assertEquals("Album", loadedTrack.getAlbum());
    }

    @Test
    void loadOnEmptyFileReturnsNullSession() throws Exception {
        // Arrange: create an empty temp file
        File tmp = File.createTempFile("musemix-empty-session", ".json");
        tmp.deleteOnExit();

        JsonSessionRepository repo = new JsonSessionRepository();

        // Act: GSON returns null when there is no JSON content
        Session loaded = repo.load(tmp);

        // Assert: for an empty file we just get null back
        assertNull(loaded);
    }
}
