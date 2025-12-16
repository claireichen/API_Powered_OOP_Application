# MuseMix – Music Recommender & Generator  
## Project Report

## 1. Overview

MuseMix is a desktop music assistant built in Java and Swing that can:

- Recommend tracks from Spotify based on **mood**, **genre**, or **artist**.
- Generate an **instrumental music clip** using MusicAPI/Suno-style generation.
- Save and load user sessions (query + track list) as JSON.
- Handle slow or failing APIs without freezing the UI.

The app is designed to demonstrate:

- All **four OOP pillars** (encapsulation, inheritance, polymorphism, abstraction).
- Multiple **design patterns** (Strategy, Factory, Observer, Singleton, Repository).
- Clean **MVC architecture** with separate model, view, controller, service, and repository layers.
- Real, asynchronous **API integration** with error handling, timeouts, and retries.

---

## 2. SYSTEM_DESIGN

### 2.1 High-Level Architecture

**Layers**

- **view/**
  - `MainFrame` (top-level window)
  - `SearchPanel`, `ResultPanel`, `GenerationPanel`, `StatusBar`
- **controller/**
  - `MainController`
  - `MusicEvent`, `MusicEventSource`, `MusicEventListener`, `EventType`
- **model/**
  - `AppModel`, `APIClient`
  - `domain/` → `UserQuery`, `Track`, `Session`, `GenerationResult`
  - `strategy/` → recommendation & generation strategies
  - `repository/` → `JsonSessionRepository`
- **service/**
  - `SpotifyService`, `SunoService`
  - `MusicServiceFactory`
  - `SessionPersistenceService`

**Data flow**

1. **User Action (View)**  
   - User enters a query / mode and clicks **Get Recommendations** or **Generate Instrumental**.
   - `SearchPanel` / `GenerationPanel` calls methods on `MainController`.

2. **Controller → Service / Strategy (Model layer)**  
   - `MainController` determines which **strategy** to use via `MusicServiceFactory`.
   - A `SwingWorker` is started so the UI thread remains responsive.
   - The strategy calls the appropriate service (`SpotifyService` or `SunoService`).

3. **Service → APIClient → External APIs**  
   - `SpotifyService` uses `APIClient.getOrRefreshSpotifyToken()` to get a Bearer token, then hits `https://api.spotify.com/v1/search`.
   - `SunoService` / `MusicAPI` uses `APIClient.postJsonWithRetry()` to create and poll a generation task.

4. **Response → Model → Controller**  
   - Services convert JSON responses into domain objects (`Track`, `GenerationResult`).
   - `MainController` updates `AppModel` (current tracks, last query, last generation result).

5. **Model → Controller → View (Observer pattern)**  
   - `MainController` fires `MusicEvent`s (e.g., `RECOMMENDATION_STARTED`, `RECOMMENDATION_COMPLETED`, `ERROR`).
   - `MainFrame` implements `MusicEventListener` and updates panels and status bar accordingly.

6. **Persistence**  
   - When a user chooses **Save Session**, `MainController` → `SessionPersistenceService.saveCurrentSession(model, file)` → `JsonSessionRepository.save(session, file)`.
   - For **Load Session**, the same flow is reversed and the loaded session is applied to `AppModel`, then broadcast to the UI.

### 2.2 Asynchronous Behavior

- Both recommendation and generation use **`SwingWorker`**:
  - `MainController.requestRecommendations(...)`
  - `MainController.requestGeneration(...)`
- `doInBackground()` makes the API call; `done()` inspects the result on the EDT.
- `MainController` keeps a reference to the current worker (`currentWorker`) to support **Cancel**:
  - `cancelCurrentOperation()` calls `currentWorker.cancel(true)` and fires a `*_CANCELLED` event.
- The **Cancel** button is enabled while work is in progress and disabled again when the worker completes.

### 2.3 Error Handling & Resiliency

**APIClient**

- Reads configuration from `config.properties` (loaded once in the singleton).
- Uses `HttpClient` with a configurable timeout (`api.timeout.ms`).
- Provides `getWithRetry(...)` and `postJsonWithRetry(...)` with:
  - `api.maxRetries` (default 2)
  - `api.backoff.ms` (default 1000 ms; linear backoff).

**Spotify / MusicAPI errors**

- Non-200 statuses throw `IOException` with the **HTTP status** and the **raw JSON body** included.
- These exceptions bubble to `MainController`, which fires `MusicEvent.error(e)`.
- `MainFrame` shows a user-friendly dialog while echoing an abbreviated error to the `StatusBar`.

**Special cases**

- 401 / invalid token from Spotify → next request will re-fetch the token.
- MusicAPI “not_ready” responses are handled with polling and a timeout:
  - If the track never finishes within N attempts, the status shows “timeout” but the user still gets the **audio URL** if one was returned.
- Errors during save/load (I/O errors, malformed JSON) are surfaced via `MusicEvent.error`.

### 2.4 Configuration & Security

- `resources/config.properties.example` (template only, committed)
  - `spotify.clientId=...`
  - `spotify.clientSecret=...`
  - `spotify.tokenUrl=https://accounts.spotify.com/api/token`
  - `suno.baseUrl=...`
  - `suno.apiKey=...`
  - `api.timeout.ms=8000`
  - `api.maxRetries=2`
  - `api.backoff.ms=1000`
- Real `config.properties` is **ignored** via `.gitignore`:
  - `config.properties`
  - `*.log`
  - `*.db`
- API keys are never logged. Error messages may include HTTP status and anonymized JSON, but not credentials.

---

## 3. Design Patterns

### 3.1 Strategy Pattern

**Where:**

- `model/strategy/RecommendationStrategy` (interface)
  - `GenreRecommendationStrategy`
  - `MoodRecommendationStrategy`
  - `ArtistSeedRecommendationStrategy`
- `model/strategy/MusicGenerationStrategy` (interface)
  - `InstrumentalGenerationStrategy` (delegates to `SunoService` / MusicAPI)

**Why:**

- Recommendations differ by **mode** (mood, genre, artist), but the controller just wants “recommendations for this `UserQuery`”.
- Generation may eventually have more modes (e.g., vocal, different styles) without changing the controller.

**How it’s used:**

- `MainController.requestRecommendationsFromUI(query, comboLabel)` → resolves a `RecommendationMode`.
- `MusicServiceFactory.createRecommendationStrategy(mode)` returns the appropriate strategy.
- `MainController.requestRecommendations` simply calls `strategy.getRecommendations(query)`.

### 3.2 Factory Pattern

**Where:**

- `MusicServiceFactory`
  - `createRecommendationStrategy(RecommendationMode mode)`
  - `createGenerationStrategy(GenerationMode mode)`

**Why:**

- Centralizes creation of strategies and injects the correct service dependencies (`SpotifyService`, `SunoService`).
- Helps keep `MainController` small and independent of concrete strategy classes.

### 3.3 Observer Pattern

**Where:**

- `MusicEventSource` (subject)
- `MusicEventListener` (observer interface)
- `MusicEvent` + `EventType` (data)
- `MainController` extends `MusicEventSource`
- `MainFrame` implements `MusicEventListener`

**Why:**

- The controller does **not** talk to Swing components directly.
- Views subscribe to events and decide how to update themselves:
  - `RECOMMENDATION_STARTED` → status “Fetching recommendations…”
  - `RECOMMENDATION_COMPLETED` → `ResultPanel.setTracks(...)`
  - `GENERATION_STARTED`, `GENERATION_COMPLETED`, `GENERATION_CANCELLED`
  - `SESSION_SAVED`, `SESSION_LOADED`
  - `ERROR` → popup dialog + error message in status bar.

### 3.4 Singleton Pattern

**Where:**

- `APIClient` (classic lazy, thread-safe singleton)

**Why:**

- Only one HTTP client and config set is needed.
- Token cache for Spotify lives here; avoids retrieving tokens in multiple places.
- Makes it trivial to share timeouts and retry behavior across services.

### 3.5 Repository Pattern

**Where:**

- `JsonSessionRepository`

**Why:**

- Encapsulates JSON file handling (`save(Session, File)`, `load(File)`).
- `SessionPersistenceService` and `MainController` don’t depend on how sessions are stored (JSON vs DB); they only interact through domain objects.

---

## 4. OOP Four Pillars

### 4.1 Encapsulation

- Domain classes keep fields `private` and expose **getters/setters with validation**:
  - `UserQuery`, `Track`, `Session`, `GenerationResult`.
- `AppModel` manages state (`currentTracks`, `lastQuery`, `lastGenerationResult`) and hides internal collections:
  - Controllers only interact via methods like `setCurrentTracks(...)`, `getCurrentTracks()`.
- `APIClient` encapsulates low-level HTTP logic, configuration, and token cache:
  - Services never manipulate `HttpClient` directly.

### 4.2 Inheritance

- `AbstractRecommendationStrategy` is a base class for:
  - `GenreRecommendationStrategy`
  - `MoodRecommendationStrategy`
  - `ArtistSeedRecommendationStrategy`
- All share the same dependency (`SpotifyService`) and common behavior, while overriding mode-specific logic.
- `MusicEventSource` is a reusable base class that holds listeners and firing logic; `MainController` **inherits** from it.

### 4.3 Polymorphism

- `RecommendationStrategy` and `MusicGenerationStrategy` are interfaces.
  - `MainController` uses them polymorphically:
    ```java
    RecommendationStrategy strategy = factory.createRecommendationStrategy(mode);
    List<Track> tracks = strategy.getRecommendations(query);
    ```
- `MusicEventListener` is implemented by `MainFrame` (and could later be implemented by logging observers, etc.).
- `toString()` is overridden in some strategies (e.g., `InstrumentalGenerationStrategy`) to help with debugging.

### 4.4 Abstraction

- Interfaces and abstractions hide details:
  - **Strategy interfaces** hide which specific Spotify query or Suno/MusicAPI endpoint is used.
  - `SessionPersistenceService` hides JSON details behind simple methods:
    - `saveCurrentSession(AppModel model, File file)`
    - `loadSessionInto(AppModel model, File file)`
  - `APIClient` hides HTTP header construction, token parsing, and retry policies.

---

## 5. Testing

I implemented **13 JUnit 5 tests** that focus on logic, patterns, and error paths instead of hitting real APIs.

### 5.1 Core Logic & Patterns

- **`APIClientSingletonTest`**
  - Verifies `APIClient.getInstance()` always returns the same instance.

- **`MusicServiceFactoryTest`**
  - Asserts each `RecommendationMode` returns the correct concrete strategy type.
  - Asserts `GenerationMode.INSTRUMENTAL` returns `InstrumentalGenerationStrategy`.

- **`MoodRecommendationStrategyTest`** (with fake Spotify service)
  - Uses a local `FakeSpotifyService` subclass to avoid network calls.
  - Ensures that when `mood` is set, the strategy passes the **mood string** as the query text.
  - Confirms that exactly one fake track is returned and that the routed query is correct.

### 5.2 Parsing & Error Handling

- **`SpotifyServiceParsingTest`**
  - Feeds a fixed JSON snippet into `parseTracksFromSearch`.
  - Verifies:
    - Track count matches JSON items.
    - Fields `name`, `artist`, `album`, `previewUrl` are correctly extracted.
    - Handles missing or null fields without crashing.

- **`SpotifyServiceRecommendationRoutingTest`**
  - Verifies that:
    - `getRecommendationsForMood()` uses `query.getMood()` when present.
    - `getRecommendationsForGenre()` uses `query.getGenre()` when present.
    - `getRecommendationsForArtist()` uses `query.getArtist()` when present.
  - If those fields are blank, it falls back to the base `query.getText()`.

### 5.3 Persistence & Repository

- **`JsonSessionRepositoryTest`**
  - Creates a `Session` with a `UserQuery` and a single `Track`.
  - Saves it to a temp file, then loads it using the same repository.
  - Asserts:
    - Loaded query text equals “lofi chill”.
    - Loaded track list has size 1.
    - Track fields (id, name, artist, album) are preserved.

- **`SessionPersistenceServiceTest`**
  - Uses a real `JsonSessionRepository` with a temp file.
  - Calls `saveCurrentSession(model, file)` then `loadSessionInto(model, file)`.
  - Asserts:
    - `AppModel.getLastQuery()` and `AppModel.getCurrentTracks()` are restored correctly.

### 5.4 UI / Controller Adjacent

Although I did not build full integration tests for the Swing UI, the event flow is indirectly tested:

- `SessionPersistenceService` and `JsonSessionRepository` ensure save/load correctness.
- Strategy routing tests ensure the controller’s mode selection is meaningful.

---

## 6. API Integration Details

### 6.1 Spotify Web API

- **Flow**
  1. `SpotifyService.searchTracks(UserQuery)` calls `APIClient.getOrRefreshSpotifyToken()`.
  2. `APIClient`:
     - Reads client ID/secret and token URL from `config.properties`.
     - Uses Client Credentials flow:
       - `POST https://accounts.spotify.com/api/token`
       - `Authorization: Basic base64(clientId:clientSecret)`
       - Body: `grant_type=client_credentials`
     - Parses `access_token` and `expires_in` manually from JSON.
     - Caches token + expiration in memory.
  3. `SpotifyService` calls:
     - `GET https://api.spotify.com/v1/search?query=...&type=track&limit=10`
     - With `Authorization: Bearer <accessToken>`.

- **Resilience**
  - `APIClient.getWithRetry(...)` wraps the GET call with retries and backoff.
  - Non-200 codes produce IOExceptions with status and error message.

### 6.2 MusicAPI / Suno-style Generation

- Uses a two-step flow:

  1. **Create task** (`POST /v1/generations` or similar)
     - Body includes prompt, genre, mood, and `instrumental=true`.
     - Returns a `taskId` and possibly an immediate audio URL.
  2. **Poll task** (`GET /v1/tasks/{id}`)
     - Polls until status is `completed` or `error`, or until a max poll limit is hit.
     - Uses `postJsonWithRetry` and `getWithRetry` for resilience.

- The result is wrapped in `GenerationResult` (status + audio URL), which the UI displays as:
  - Status text in the `StatusBar`.
  - A clickable “open audio” link that opens the system browser.

---

## 7. Challenges I Faced & How I Solved Them

### 7.1 Spotify Token & 401 Errors

**Problem:**  
Initially I received *“Invalid access token”* errors (HTTP 401) because the token was not being refreshed correctly and some configuration keys were missing.

**Solution:**

- Centralized token logic in `APIClient.getOrRefreshSpotifyToken()` with:
  - Config-based clientId/secret and token URL.
  - An in-memory token cache with `Instant` expiry.
- Added explicit checks:
  ```java
  if (clientId == null || clientSecret == null) {
      throw new IllegalStateException("Spotify clientId/clientSecret not set in config.properties");
  }

**Learned**

Always separate token acquisition from normal API calls, and fail fast if configuration is missing. 

--- 

## 8. AI Usage Disclosure

I used ChatGPT (OpenAI) as an assistant throughout this project. 

What I asked for/used it for:
- Help designing the initial class skeletons.
- Guidance on implementation.
- Suggestions for organizing MVC structure and choosing & applying design patterns.
- Assistance in writing some JUnit tests.
