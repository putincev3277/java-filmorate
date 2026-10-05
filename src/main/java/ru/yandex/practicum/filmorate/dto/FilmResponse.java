package ru.yandex.practicum.filmorate.dto;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

public record FilmResponse(
        Long id,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration,
        Mpa mpa,
        List<Genre> genres,
        List<Long> likes
) {}
