package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {

    Film add(Film film);

    Film findById(Long id);

    Film update(Long id, Film film);

    Film delete(Long id);

    List<Film> findAll();
}
