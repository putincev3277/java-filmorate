package ru.yandex.practicum.filmorate.service;



import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film createFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film getFilm(Long id) {
        Film film = filmStorage.findById(id);
        if (film == null) {
            throw new FilmNotFoundException("Фильм не найден");
        }
        return film;
    }

    public Film updateFilm(Long id, Film film) {
        Film updatedFilm = filmStorage.update(id, film);
        if (updatedFilm == null) {
            throw new FilmNotFoundException("Фильм не найден");
        }
        return updatedFilm;
    }

    public void deleteFilm(Long id) {
        Film film = filmStorage.delete(id);
        if (film == null) {
            throw new FilmNotFoundException("Фильм не найден");
        }
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }
}
