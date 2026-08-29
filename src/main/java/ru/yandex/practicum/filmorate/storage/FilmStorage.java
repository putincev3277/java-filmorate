package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> add(Film film);

    Optional<Film> findById(Long id);

    Film update(Long id, Film film);

    Film delete(Long id);

    List<Film> findAll();
}

