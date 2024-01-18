package advisor;

import enums.Input;
import http.Authorization;
import http.RESTAPIHandler;
import http.Server;
import models.Pagination;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        Authorization authorization;
        RESTAPIHandler dataHandler;
        String nElementsPagination;
        Pagination<?> pagination = null;
        final List<String> argsList = Arrays.asList(args);
        if (argsList.contains("-access")) {
            try {
                authorization = new Authorization(argsList.get(
                        argsList.indexOf("-access") + 1
                ));
            } catch (IndexOutOfBoundsException e) {
                authorization = new Authorization();
            }
        } else {
            authorization = new Authorization();
        }
        if (argsList.contains("-resource")) {
            try {
                dataHandler = new RESTAPIHandler(argsList.get(
                        argsList.indexOf("-resource") + 1
                ));
            } catch (IndexOutOfBoundsException e) {
                dataHandler = new RESTAPIHandler();
            }
        } else {
            dataHandler = new RESTAPIHandler();
        }
        if (argsList.contains("-page")) {
            try {
                nElementsPagination = argsList.get(argsList.indexOf("-page") + 1);
            } catch (IndexOutOfBoundsException e) {
                nElementsPagination = null;
            }
        } else {
            nElementsPagination = null;
        }
        while (scanner.hasNextLine()) {
            final String[] inputs = scanner.nextLine().split(" ");
            Input input = Input.valueOf(inputs[0].toUpperCase());
            switch (input) {
                case NEW -> {
                    if (dataHandler.getAccessToken() != null) {
                        if (nElementsPagination != null) {
                            pagination = new Pagination<>(dataHandler.getNewReleases(), nElementsPagination);
                        } else {
                            pagination = new Pagination<>(dataHandler.getNewReleases());
                        }
                        pagination.printCurrentPage();
                    } else {
                        System.out.println("Please, provide access for application.");
                    }
                }
                case FEATURED -> {
                    if (dataHandler.getAccessToken() != null) {
                        if (nElementsPagination != null) {
                            pagination = new Pagination<>(dataHandler.getFeaturedPlaylists(), nElementsPagination);
                        } else {
                            pagination = new Pagination<>(dataHandler.getFeaturedPlaylists());
                        }
                        pagination.printCurrentPage();
                    } else {
                        System.out.println("Please, provide access for application.");
                    }
                }
                case PLAYLISTS -> {
                    if (dataHandler.getAccessToken() != null) {
                        StringBuilder playlistCategoryName = new StringBuilder(inputs[1]);
                        for (int i = 2; i < inputs.length; i++) {
                            playlistCategoryName.append(" %s".formatted(inputs[i]));
                        }
                        try {
                            if (nElementsPagination != null) {
                                pagination = new Pagination<>(
                                        dataHandler.getCategoryPlaylists(
                                                playlistCategoryName.toString()), nElementsPagination
                                );
                            } else {
                                pagination = new Pagination<>(
                                        dataHandler.getCategoryPlaylists(playlistCategoryName.toString())
                                );
                            }
                            pagination.printCurrentPage();
                        } catch (NullPointerException e) {
                            System.out.println("Unknown category name.");
                        }
                    } else {
                        System.out.println("Please, provide access for application.");
                    }
                }
                case CATEGORIES -> {
                    if (dataHandler.getAccessToken() != null) {
                        if (nElementsPagination != null) {
                            pagination = new Pagination<>(dataHandler.getCategories(), nElementsPagination);
                        } else {
                            pagination = new Pagination<>(dataHandler.getCategories());
                        }
                        pagination.printCurrentPage();
                    } else {
                        System.out.println("Please, provide access for application.");
                    }
                }
                case PREV -> {
                    if (pagination != null) {
                        pagination.printPrevPage();
                    } else {
                        System.out.println("You have not selected what to see.");
                    }
                }
                case NEXT -> {
                    if (pagination != null) {
                        pagination.printNextPage();
                    } else {
                        System.out.println("You have not selected what to see.");
                    }
                }
                case AUTH -> {
                    System.out.println("use this link to request the access code:");
                    System.out.println(authorization.getSpotifyAuthLink());
                    Server authServer = new Server(authorization);
                    authServer.createAuthorizationContext();
                    authServer.start();
                    System.out.println("waiting for code...");
                    while (authorization.getSpotifyAuthCode() == null) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    authServer.stop(1);
                    try {
                        System.out.println("making http request for access_token...");
                        HttpResponse<String> responseAccessToken = authorization.sendSpotifyAccessToken();
                        dataHandler.setAccessToken(responseAccessToken.body());
                        System.out.println("Success!");
                    } catch (Exception e) {
                        System.out.println("Spotify denied access token, will retry the request later");
                    }
                }
                case EXIT -> {
                    return;
                }
                default -> throw new IllegalStateException("Unexpected value: " + input);
            }
        }
    }
}