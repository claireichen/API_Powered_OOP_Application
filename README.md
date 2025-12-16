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
