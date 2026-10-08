package ru.yandex.practicum.filmorate.model;

public enum MpaRating {
    G(1, "G", "General audiences"),
    PG(2, "PG", "Parental guidance suggested"),
    PG_13(3, "PG-13", "Parents strongly cautioned"),
    R(4, "R", "Restricted"),
    NC_17(5, "NC-17", "Adults only");

    private final long id;
    private final String name;          // <-- добавили
    private final String description;

    MpaRating(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getName() {           // <-- добавили геттер
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static MpaRating valueOfId(long id) {
        for (MpaRating rating : values()) {
            if (rating.id == id) {
                return rating;
            }
        }
        throw new IllegalArgumentException("MPA rating with id " + id + " not found");
    }
}
