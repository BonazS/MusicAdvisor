package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

public class Server {

    private HttpServer server;
    private final Authorization authorization;
    private static final String CONFIRM_AUTH_MESSAGE = "Got the code. Return back to your program.";
    private static final String REJECT_AUTH_MESSAGE = "Authorization code not found. Try again.";

    public Server(Authorization authorization) {
        this.authorization = authorization;
        try {
            server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
        } catch (BindException e) {
            server = null;
            System.out.println(e.getMessage());
        } catch (IOException e) {
            server = null;
            System.out.println("Server is not created.");
        }
    }

    public void createAuthorizationContext() {
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String queryAuthCode = exchange.getRequestURI().getQuery();
                if (queryAuthCode != null && queryAuthCode.contains("code")) {
                    System.out.println("code received");
                    String[] authCode = queryAuthCode.split("=");
                    authorization.setSpotifyAuthCode(authCode[1]);
                    exchange.sendResponseHeaders(200, CONFIRM_AUTH_MESSAGE.length());
                    exchange.getResponseBody().write(CONFIRM_AUTH_MESSAGE.getBytes());
                    exchange.getResponseBody().close();
                } else {
                    System.out.println("code not received");
                    exchange.sendResponseHeaders(200, REJECT_AUTH_MESSAGE.length());
                    exchange.getResponseBody().write(REJECT_AUTH_MESSAGE.getBytes());
                    exchange.getResponseBody().close();
                }
            }
        });
    }

    public void start() { server.start(); }

    public void stop(final int delay) { server.stop(delay); }
}