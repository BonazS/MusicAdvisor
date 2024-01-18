package models;

import java.net.URI;

public class Playlist<T> {

    private final String name;
    private final URI link;
    private final T reference;

    public Playlist(final String name, final URI link) {
        this(name, link, null);
    }

    public Playlist(final String name, final URI link, final T reference) {
        this.name = name;
        this.link = link;
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public URI getLink() {
        return link;
    }

    public T getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return new String(new StringBuilder(name).append("\n%s\n".formatted(link)));
    }
}