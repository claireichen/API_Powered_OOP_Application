package org.example.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Properties;

public class APIClient {

    private static APIClient instance;

    private final HttpClient httpClient;
    private final Properties config;

    // Spotify token cache
    private String spotifyAccessToken;
    private Instant spotifyTokenExpiry; // epoch time when it expires

    private APIClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.config = loadConfig();
    }

    public static synchronized APIClient getInstance() {
        if (instance == null) {
            instance = new APIClient();
        }
        return instance;
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream in = APIClient.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("config.properties not found on classpath. " +
                        "Copy config.properties and fill in your keys.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public Properties getConfig() {
        return config;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    /* ------------------------------------------------------------------
       Spotify token helpers (Client Credentials Flow)
       ------------------------------------------------------------------ */

    public synchronized String getOrRefreshSpotifyToken() throws IOException, InterruptedException {
        if (spotifyAccessToken != null && spotifyTokenExpiry != null) {
            // add 30s safety margin
            if (Instant.now().isBefore(spotifyTokenExpiry.minusSeconds(30))) {
                return spotifyAccessToken;
            }
        }
        requestSpotifyToken();
        return spotifyAccessToken;
    }

    private void requestSpotifyToken() throws IOException, InterruptedException {
        String clientId = config.getProperty("spotify.clientId");
        String clientSecret = config.getProperty("spotify.clientSecret");

        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Spotify clientId/clientSecret not set in config.properties");
        }

        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        String body = "grant_type=client_credentials";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get Spotify token: HTTP " + response.statusCode()
                    + " - " + response.body());
        }

        // super lightweight JSON parsing without adding a library:
        // we just grab "access_token" and "expires_in" with manual parsing
        String json = response.body();

        String token = extractJsonString(json, "access_token");
        long expiresIn = extractJsonLong(json, "expires_in");

        if (token == null || expiresIn <= 0) {
            throw new IOException("Could not parse Spotify token response: " + json);
        }

        this.spotifyAccessToken = token;
        this.spotifyTokenExpiry = Instant.now().plusSeconds(expiresIn);
    }

    // Very naive JSON extractors – good enough for this limited use.
    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = json.indexOf('"', start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private long extractJsonLong(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) return -1;
        int start = idx + pattern.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* ------------------------------------------------------------------
       Suno helpers
       ------------------------------------------------------------------ */

    public String getSunoBaseUrl() {
        return config.getProperty("suno.baseUrl");
    }

    public String getSunoApiKey() {
        return config.getProperty("suno.apiKey");
    }

    /* ------------------------------------------------------------------
       Generic GET/POST helpers
       ------------------------------------------------------------------ */

    public HttpResponse<String> get(String url, String bearerToken)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        HttpRequest request = builder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> postForm(String url, String formBody, String authHeader)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody));

        if (authHeader != null && !authHeader.isBlank()) {
            builder.header("Authorization", authHeader);
        }

        HttpRequest request = builder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public HttpResponse<String> postJson(String url, String jsonBody, String authToken)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        // If an auth token is provided, send it as Authorization header.
        // If it already starts with "Bearer ", we don't add it again.
        if (authToken != null && !authToken.isBlank()) {
            if (authToken.startsWith("Bearer ")) {
                builder.header("Authorization", authToken);
            } else {
                builder.header("Authorization", "Bearer " + authToken);
            }
        }

        HttpRequest request = builder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
