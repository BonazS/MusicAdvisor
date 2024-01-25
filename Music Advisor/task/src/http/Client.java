package http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public class Client {

    private final HttpClient client;
    private static final String CLIENT_URI = "http://localhost:8080";
    private static final String SPOTIFY_CLIENT_ID = "2f48afb9911d4d9baf554c513843c264";
    private static final String SPOTIFY_CLIENT_SECRET = "9fbb9e072d9c46b392a046e6f7770772";
    private static final String SPOTIFY_AUTH_LINK = String.format(
            "https://accounts.spotify.com/authorize?client_id=%s&redirect_uri=%s&response_type=code",
            SPOTIFY_CLIENT_ID,
            CLIENT_URI
    );
    private static final String SPOTIFY_ACCESS_TOKEN_LINK = "https://accounts.spotify.com/api/token";
    private static final int AUTH_TIMEOUT_MS = 1000;
    private static final int ACCESS_TOKEN_TIMEOUT_MS = 1000;
    private String accessToken;
    private String jsonData = "";

    public Client() {
        client = HttpClient.newBuilder().build();
        accessToken = "";
    }

    public String getSpotifyAuthLink() { return SPOTIFY_AUTH_LINK; }

    private void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getAccessToken() { return accessToken; }

    public HttpResponse<String> sendSpotifyAuthorization() throws IOException, InterruptedException {
        System.out.println("waiting for code...");
        HttpRequest spotifyAuthRequest = HttpRequest.newBuilder()
                .uri(URI.create(SPOTIFY_AUTH_LINK))
                .timeout(Duration.ofMillis(AUTH_TIMEOUT_MS))
                .build();
        return clientSendRequest(spotifyAuthRequest);
    }

    public HttpResponse<String> sendSpotifyAccessToken() throws IOException, InterruptedException {
        System.out.println("making http request for access_token...");
        HttpRequest spotifyAuthRequest = HttpRequest.newBuilder()
                .header("Content-Type",
                        "application/x-www-form-urlencoded")
                .header("Authorization",
                        "Basic " + Base64.getEncoder()
                                .encodeToString("%s:%s".formatted(SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET).getBytes()))
                .uri(URI.create(SPOTIFY_ACCESS_TOKEN_LINK))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .timeout(Duration.ofMillis(ACCESS_TOKEN_TIMEOUT_MS))
                .build();
        return clientSendRequest(spotifyAuthRequest);
    }

    public HttpRequest clientGETRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(CLIENT_URI))
                .build();
    }

    public HttpRequest clientPOSTRequest() {
        return HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(CLIENT_URI))
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .build();
    }

    public HttpResponse<String> clientSendRequest(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
