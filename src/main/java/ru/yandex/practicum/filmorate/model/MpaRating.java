package ru.yandex.practicum.filmorate.model;

public enum MpaRating {
    G(1, "General audiences"),
    PG(2, "Parental guidance suggested"),
    PG_13(3, "Parents strongly cautioned"),
    R(4, "Restricted"),
    NC_17(5, "Adults only");

    private final long id;
    private final String description;

    MpaRating(long id, String description) {
        this.id = id;
        this.description = description;
    }

    public long getId() {
        return id;
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
