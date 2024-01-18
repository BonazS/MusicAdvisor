package http;

import com.google.gson.*;
import models.Album;
import models.Artist;
import models.Category;
import models.Playlist;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RESTAPIHandler {

    private static final int REQUEST_TIMEOUT_MS = 5000;
    private final HttpClient handler;
    private String spotifyAPIServerPath = "https://api.spotify.com";
    private JsonObject accessToken;

    public RESTAPIHandler() {
        handler = HttpClient.newBuilder().build();
    }

    public RESTAPIHandler(final String spotifyAPIServerPath) {
        this();
        this.spotifyAPIServerPath = spotifyAPIServerPath;
    }

    public JsonObject getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String httpResponseAccessToken) {
        this.accessToken = parseStringJsonObject(httpResponseAccessToken);
    }

    public HttpResponse<String> sendHttpRequest(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = handler.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Timeout expired. Request interrupted.");
        }
        return response;
    }

    public static JsonObject parseStringJsonObject(String json) {
        JsonObject result = null;
        try {
            result = JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            System.out.println("Wrong JSON format.");
        }
        return result;
    }

    public List<Album> getNewReleases() {
        final List<Album> newReleases = new ArrayList<>();
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization",
                        "%s %s".formatted(
                                accessToken.get("token_type").getAsString(),
                                accessToken.get("access_token").getAsString()
                        )
                )
                .uri(URI.create("%s/v1/browse/new-releases".formatted(spotifyAPIServerPath)))
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                .GET()
                .build();
        final HttpResponse<String> response = sendHttpRequest(request);
        final JsonObject jsonResponse = parseStringJsonObject(response.body());
        try {
            for (JsonElement album : jsonResponse.getAsJsonObject("albums")
                    .getAsJsonArray("items")) {
                final String name = album.getAsJsonObject().get("name").getAsString();
                final List<Artist> artists = new ArrayList<>();
                for (JsonElement artist : album.getAsJsonObject().getAsJsonArray("artists")) {
                    artists.add(new Artist(artist.getAsJsonObject().get("name").getAsString()));
                }
                try {
                    final URI uri = new URI(album.getAsJsonObject()
                            .getAsJsonObject("external_urls")
                            .get("spotify")
                            .getAsString()
                    );
                    newReleases.add(new Album(name, artists, uri));
                } catch (URISyntaxException e) {
                    System.out.println("Wrong URI format.");
                }
            }
        } catch (NullPointerException e) {
            System.out.println(
                    jsonResponse.getAsJsonObject("error")
                            .get("message")
                            .getAsString()
            );
        }
        return newReleases;
    }

    public List<Category> getCategories() {
        final List<Category> categories = new ArrayList<>();
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization",
                        "%s %s".formatted(
                                accessToken.get("token_type").getAsString(),
                                accessToken.get("access_token").getAsString()
                        )
                )
                .uri(URI.create("%s/v1/browse/categories".formatted(spotifyAPIServerPath)))
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                .GET()
                .build();
        final HttpResponse<String> response = sendHttpRequest(request);
        final JsonObject jsonResponse = parseStringJsonObject(response.body());
        try {
            for (JsonElement category : jsonResponse.getAsJsonObject("categories")
                    .getAsJsonArray("items")) {
                final String name = category.getAsJsonObject().get("name").getAsString();
                final String id = category.getAsJsonObject().get("id").getAsString();
                categories.add(new Category(name, id));
            }
        } catch (NullPointerException e) {
            System.out.println(
                    jsonResponse.getAsJsonObject("error")
                            .get("message")
                            .getAsString()
            );
        }
        return categories;
    }

    public List<Playlist<String>> getFeaturedPlaylists() {
        final List<Playlist<String>> featuredPlaylists = new ArrayList<>();
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization",
                        "%s %s".formatted(accessToken.get("token_type").getAsString(),
                                accessToken.get("access_token").getAsString()
                        )
                )
                .uri(URI.create("%s/v1/browse/featured-playlists".formatted(spotifyAPIServerPath)))
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                .GET()
                .build();
        final HttpResponse<String> response = sendHttpRequest(request);
        final JsonObject jsonResponse = parseStringJsonObject(response.body());
        try {
            for (JsonElement playlist : jsonResponse.getAsJsonObject("playlists")
                    .getAsJsonArray("items")) {
                final String name = playlist.getAsJsonObject().get("name").getAsString();
                try {
                    final URI uri = new URI(playlist.getAsJsonObject()
                            .getAsJsonObject("external_urls")
                            .get("spotify")
                            .getAsString()
                    );
                    featuredPlaylists.add(new Playlist<>(name, uri));
                } catch (URISyntaxException e) {
                    System.out.println("Wrong URI format.");
                }
            }
        } catch (NullPointerException e) {
            System.out.println(
                    jsonResponse.getAsJsonObject("error")
                            .get("message")
                            .getAsString()
            );
        }
        return featuredPlaylists;
    }

    public List<Playlist<Category>> getCategoryPlaylists(final String categoryName) {
        List<Playlist<Category>> categoryPlaylists = null;
        final List<Category> categories = getCategories();
        for (Category category : categories) {
            if (category.getName().equals(categoryName)) {
                categoryPlaylists = new ArrayList<>();
                final HttpRequest request = HttpRequest.newBuilder()
                        .header("Authorization",
                                "%s %s".formatted(accessToken.get("token_type").getAsString(),
                                        accessToken.get("access_token").getAsString()
                                )
                        )
                        .uri(URI.create("%s/v1/browse/categories/%s/playlists"
                                .formatted(spotifyAPIServerPath, category.getId())))
                        .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                        .GET()
                        .build();
                final HttpResponse<String> response = sendHttpRequest(request);
                final JsonObject jsonResponse = parseStringJsonObject(response.body());
                try {
                    for (JsonElement playlist : jsonResponse.getAsJsonObject("playlists")
                            .getAsJsonArray("items")) {
                        final String name = playlist.getAsJsonObject().get("name").getAsString();
                        try {
                            final URI uri = new URI(playlist.getAsJsonObject()
                                    .getAsJsonObject("external_urls")
                                    .get("spotify")
                                    .getAsString()
                            );
                            categoryPlaylists.add(new Playlist<>(name, uri, category));
                        } catch (URISyntaxException e) {
                            System.out.println("Wrong URI format.");
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println(
                            jsonResponse.getAsJsonObject("error")
                                    .get("message")
                                    .getAsString()
                    );
                }
            }
        }
        return categoryPlaylists;
    }

    public static <E> void printResultList(List<E> result) throws NullPointerException {
        result.forEach(System.out::println);
    }
}