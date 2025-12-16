# MuseMix – Music Recommender & Generator

MuseMix is a Java + Swing desktop app that recommends music from Spotify and generates new instrumental tracks using MusicAPI.ai. It’s built as an MVC application, integrates live APIs, and showcases core OOP concepts and design patterns.

---

## Tech Stack

- **Language:** Java 17+
- **GUI:** Swing
- **Build:** Maven
- **APIs:**
  - Spotify Web API – track search & recommendations
  - MusicAPI.ai – AI music generation (instrumentals)
- **Testing:** JUnit 5

---

## Architecture Overview

MuseMix follows a classic **MVC + services + repository** layout:

- **Model (`org.example.model`)**
  - Domain: `UserQuery`, `Track`, `Session`, `GenerationResult`
  - Strategies: recommendation & generation strategies (Strategy pattern)
  - `APIClient` (Singleton) for HTTP + config + retries
  - `repository.JsonSessionRepository` for JSON session save/load
- **View (`org.example.view`)**
  - `MainFrame` (top-level JFrame)
  - `panels.SearchPanel`, `ResultPanel`, `GenerationPanel`, `StatusBar`
- **Controller (`org.example.controller`)**
  - `MainController` coordinates user actions, background work, and events
  - `MusicEventSource` / `MusicEventListener` (Observer pattern)
- **Service (`org.example.service`)**
  - `SpotifyService` (Spotify Web API)
  - `SunoService` / `MusicApiService` (MusicAPI.ai generation)
  - `MusicServiceFactory` (Factory + Strategy wiring)
- **Entry point**
  - `Main` – bootstraps `AppModel`, `MainController`, and `MainFrame`

Async work (API calls, save/load) is done in **`SwingWorker`s**, so the UI never freezes.

---

## Setup

### 1. Clone the repo

```bash
git clone https://github.com/<your-user>/<your-repo>.git
cd <your-repo>
```

### 2. Create config.properties

Inside src/main/resources, you'll find:
```bash
config.properties.example
```
Copy it to config.properties in the same folder and fill in your own keys:
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```
Then edit config.properties:
```bash
# Spotify
spotify.clientId=your-spotify-client-id
spotify.clientSecret=your-spotify-client-secret
spotify.tokenUrl=https://accounts.spotify.com/api/token

# MusicAPI.ai (Suno-like)
suno.baseUrl=https://api.musicapi.ai/v1
suno.apiKey=your-musicapi-api-key

# Generic API behavior
api.timeout.ms=8000       # HTTP client timeout in ms
api.maxRetries=2          # how many times to retry on IO error
api.backoff.ms=1000       # base backoff between retries in ms
```
Do NOT commit config.properties - it's ignored by .gitignore.

### 3. Build & run

From IntelliJ:
1. Open the Maven project.
2. Make sure Java 17+ SDK is configured.
3. Run the Main class (org.example.Main).

From the command line (optional):
```bash
mvn clean package
# then run the generated jar or Main class from your IDE
```

---

## Features

### Recommendations tab

- Search by query/mood/artist.
- Choose a mode:
  - Mood - uses UserQuery.mood first,  otherwise falls back to text.
  - Genre - uses UserQuery.genre when present.
  - Artist - uses UserQuery.artist when present.
- Displays tracks in a table (Title, Artist, Album).
- Preview URL is stored in the Track model for potential future use.
- Cancel button stops an in-flight request via SwingWorker.cancel(true).

### Generation tab

- Enter:
  - Prompt - description of the music (e.g., "lofi chill")
  - Optional Genre and Mood
- Click Generate Instrumental to:
  1. Create a generation task via MusicAPI.ai.
  2. Poll the task until completed or timeout.
- Status label shows:
  - Starting generation...
  - Polling task...
  - Generation complete / timeout / friendly error.
- Clickable link to open the generated audio URL in the browser.
- Cancel button interrupts the polling worker.

### Sessions

- Save session (menu File -> Save Session...):
  - Saves current UserQuery and List<Track> to a JSON file via JsonSessionRepository.
- Load session (menu File -> Load Session...):
  - Loads a previous session and repopulates the results table.

### Status & Errors

- StatusBar shows short messages (Ready, Fetching recommendations..., Generating music...).
- API errors (401, 403, network failure, out-of-credits) show:
  - A descriptive message in the status bar.
  - A Swing error dialog with the exception message truncated for readability.

---

## Design Patterns

### Strategy

Used for swappable recommendation and generation behavior.
- RecommendationStrategy (interface)
  - MoodRecommendationStrategy
  - GenreRecommendationStrategy
  - ArtistSeedRecommendationStrategy
- MusicGenerationStrategy (interface)
  - InstrumentalGenerationStrategy (delegates to MusicAPI.ai)

The controller selects a strategy at runtime based on the chosen mode and calls either strategy.getRecommendations(query) or strategy.generate(query).

### Factory

MusicServiceFactory creates the right strategy for a given mode:
- createRecommendationStrategy(RecommendationMode mode)
- createGenerationStrategy(GenerationMode mode)

This keeps MainController simple and makes strategy wiring easy to change in one place.

### Observer

Custom event system used to keep the UI responsive and decoupled:
- MusicEventSource - base class for firing events
- MusicEventListener - interface implemented by views
- MusicEvent - event object with type + optional payload
- EventType - RECOMMENDATION_STARTED, RECOMMENDATION_COMPLETED, GENERATION_STARTED, GENERATION_COMPLETED, ERROR, etc.

MainController fires events; MainFrame / ResultPanel / StatusBar listen and update the UI accordingly.

### Singleton

APIClient is a classic Singleton:
- private static APIClient instance
- public static synchronized APIClient getInstance()
- Centralizes:
  - HttpClient configuration (timeouts)
  - Config loading (config.properties)
  - Spotify token caching (access token + expiry)
  - Retry helpers (getWithRetry, postJsonWithRetry)

---

## OOP Four Pillars

### Encapsulation

- Domain classes (UserQuery, Track, Session, GenerationResult) use private fields with getters/setters.
- APIClient hides all HTTP/Spotify/MusicAPI details behind methods like:
  - getOrRefreshSpotifyToken()
  - getWithRetry(...)
  - postJsonWithRetry(...)

### Inheritance

- AbstractRecommendationStrategy provides a base for common logic:
  - Holds a SpotifyService reference.
  - Shared helper methods.
- Concrete strategies extend it for mood/genre/artist behaviors.

### Polymorphism

- RecommendationStrategy and MusicGenerationStrategy interfaces:
  - The controller talks to them via the interface type.
  - MusicServiceFactory can return any implementation without the caller knowing which one.
- Overriden toString() in strategy classes helps with debugging/logging.

### Abstraction

- Service interfaces/abstract base classes hide implementation details:
  - RecommendationStrategy / MusicGenerationStrategy
  - JsonSessionRepository abstracts JSON persistence from the rest of the app.
 
More detailed discussion of OOP usage in REPORT.md. 

---

## Testing

All tests are in src/test/java/org/example and use JUnit 5. 

Currently 13 tests cover:
- Singleton behavior
  - APIClientSingletonTest
- Factory & Strategy wiring
  - MusicServiceFactoryTest - ensures correct strategy types are returned for each mode.
- Strategy behavior (polymorphism)
  - MoodRecommendationStrategyTest
    - Uses a fake SpotifyService to assert that:
      - Mood text is preferred when present.
      - No real network calls are made in the test.
- Spotify JSON parsing
  - SpotifyServiceParsingTest - parses a sample JSON and checks fields in Track.
- Recommendation routing
  - SpotifyServiceRecommendationRoutingTest - tests that mood/genre/artist routing uses the correct UserQuery field.
- Persistence
  - JsonSessionRepositoryTest
    - saveAndLoadRoundTrip - verifies session survives a write/read cycle
    - loadOnEmptyFileReturnsNullSession - documents behavior for empty files.

Run tests via IntelliJ or:
```bash
mvn test
```

--- 

## Resilient API Behavior

Musemix is designed to be gentle on APIs and resilient to failure:
- Configurable timeouts
  - APIClient  reads api.timeout.ms from config.properties.
  - Configured via HttpClient.newBuilder().connectTimeout(...).
- Retries with backoff
  - getWithRetry(...) and postJsonWithRetry(...) use:
    - api.maxRetries
    - api.backoff.ms (with simple incremental backoff)
  - Only IO-level failures are retried; HTTP 4xx/5xx are surfaced immediately.
- Token caching
  - Spotify access token + expiry are cached in APIClient.
  - Avoids redundant token requests and handles expiration gracefully.
- Error handling
  - 401 / 403 / invalid credentials
  - MusicAPI "credits exhausted" error
  - Network failures/timeouts
  - All are caught, surfaced via:
    - Status bar text
    - Swing error dialogs

---

## Folder Structure

```bash
src/
  main/
    java/
      org/example/
        controller/
          MainController.java
          EventType.java
          MusicEvent.java
          MusicEventListener.java
          MusicEventSource.java
        model/
          APIClient.java
          AppModel.java
          domain/
            UserQuery.java
            Track.java
            Session.java
            GenerationResult.java
          repository/
            JsonSessionRepository.java
          strategy/
            AbstractRecommendationStrategy.java
            RecommendationStrategy.java
            MoodRecommendationStrategy.java
            GenreRecommendationStrategy.java
            ArtistSeedRecommendationStrategy.java
            MusicGenerationStrategy.java
            InstrumentalGenerationStrategy.java
        service/
          MusicServiceFactory.java
          SpotifyService.java
          SunoService.java   # MusicAPI.ai wrapper
        view/
          MainFrame.java
          panels/
            SearchPanel.java
            ResultPanel.java
            GenerationPanel.java
            StatusBar.java
        Main.java
    resources/
      config.properties.example
      # config.properties (local only, in .gitignore)

  test/
    java/
      org/example/
        model/
          APIClientSingletonTest.java
          repository/
            JsonSessionRepositoryTest.java
          strategy/
            MoodRecommendationStrategyTest.java
        service/
          MusicServiceFactoryTest.java
          SpotifyServiceParsingTest.java
          SpotifyServiceRecommendationRoutingTest.java
```

---

### Demo

https://youtu.be/mBYKriKmGCU 
