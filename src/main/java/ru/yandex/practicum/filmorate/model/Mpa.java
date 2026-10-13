package ru.yandex.practicum.filmorate.model;

public record Mpa(Long id, String name) {

    public static Mpa fromMpaRating(MpaRating rating) {
        if (rating == null) {
            return null;
        }
        return new Mpa(rating.getId(), rating.getName());
    }
}
