package enums;

public enum Category {
    TOPLISTS("Top Lists"),
    POP("Pop"),
    MOOD("Mood"),
    LATIN("Latin");

    private final String name;

    Category(String name){this.name = name;}

    public String getName() {
        return name;
    }
}
