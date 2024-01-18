package models;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Album {

    private final String name;
    private final List<Artist> artists;
    private final URI link;

    public Album(String name) {
        this(name, new ArrayList<>(), null);
    }

    public Album(String name, List<Artist> artists, URI link) {
        this.name = name;
        this.artists = artists;
        this.link = link;
    }

    public URI getLink() {
        return link;
    }

    @Override
    public String toString() {
        StringBuilder playlistString = new StringBuilder(name);
        if (!artists.isEmpty()) {
            playlistString.append("\n[");
            for (int i = 0; i < artists.size() - 1; i++) {
                playlistString.append(artists.get(i).getName()).append(", ");
            }
            playlistString.append(artists.get(artists.size() - 1).getName()).append("]");
        }
        playlistString.append("\n%s\n".formatted(link));
        return new String(playlistString);
    }
}