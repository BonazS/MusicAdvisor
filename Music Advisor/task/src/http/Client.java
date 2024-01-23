package http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Client {

    private HttpClient client;
    private static final String CLIENT_URI = "http://localhost:8080";
    private String jsonData = "";

    public Client() {
        client = HttpClient.newBuilder().build();
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

    public HttpResponse<String> clientResponse(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
