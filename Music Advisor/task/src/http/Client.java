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
    private String spotifyAccessPoint = "https://accounts.spotify.com";
    private final String CLIENT_URI = "http://localhost:8080";
    private static final String SPOTIFY_CLIENT_ID = "2f48afb9911d4d9baf554c513843c264";
    private static final String SPOTIFY_CLIENT_SECRET = "9fbb9e072d9c46b392a046e6f7770772";
    private final String SPOTIFY_AUTH_LINK = String.format(
            "%s/authorize?client_id=%s&redirect_uri=%s&response_type=code",
            spotifyAccessPoint,
            SPOTIFY_CLIENT_ID,
            CLIENT_URI
    );
    private final String SPOTIFY_ACCESS_TOKEN_LINK = String.format("%s/api/token", spotifyAccessPoint);
    private static final int AUTH_TIMEOUT_MS = 10000;
    private static final int ACCESS_TOKEN_TIMEOUT_MS = 10000;
    private String spotifyAuthCode;
    private String spotifyAccessToken;
    private String jsonData = "";

    public Client() {
        client = HttpClient.newBuilder().build();
    }

    public Client(String spotifyAccessPoint) {
        this();
        this.spotifyAccessPoint = spotifyAccessPoint;
    }

    public String getSpotifyAuthLink() { return SPOTIFY_AUTH_LINK; }

    public String getSpotifyAuthCode() { return spotifyAuthCode; }

    public void setSpotifyAuthCode(String spotifyAuthCode) { this.spotifyAuthCode = spotifyAuthCode; }

    public void setAccessToken(String accessToken) { this.spotifyAccessToken = accessToken; }

    public String getAccessToken() { return spotifyAccessToken; }

    public HttpResponse<String> sendSpotifyAuthorization() throws IOException, InterruptedException {
        System.out.println("waiting for code...");
        HttpRequest spotifyAuthRequest = HttpRequest.newBuilder()
                .uri(URI.create(SPOTIFY_AUTH_LINK))
                .timeout(Duration.ofMillis(AUTH_TIMEOUT_MS))
                .build();
        HttpResponse<String> response = clientSendRequest(spotifyAuthRequest);
        System.out.println(response.statusCode());
        System.out.println(response.body());
        return response;
    }

    public HttpResponse<String> sendSpotifyAccessToken() throws IOException, InterruptedException {
        // System.out.println("making http request for access_token...");
        HttpRequest spotifyAccessTokenRequest = HttpRequest.newBuilder()
                .header("Content-Type",
                        "application/x-www-form-urlencoded")
                .header("Authorization",
                        "Basic " + Base64.getEncoder()
                                .encodeToString("%s:%s".formatted(SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET).getBytes()))
                .uri(URI.create(SPOTIFY_ACCESS_TOKEN_LINK))
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format(
                                "code=%s&redirect_uri=%s&grant_type=authorization_code",
                                getSpotifyAuthCode(),
                                CLIENT_URI)
                        )
                )
                .timeout(Duration.ofMillis(ACCESS_TOKEN_TIMEOUT_MS))
                .build();
        return clientSendRequest(spotifyAccessTokenRequest);
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
