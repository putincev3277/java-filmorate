package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class FilmService {
    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film createFilm(Film film) {
        validateReleaseDate(film.getReleaseDate());
        validateDescription(film.getDescription());
        validateDuration(film.getDuration());
        return filmStorage.add(film)
                .orElseThrow(() -> new RuntimeException("Не удалось сохранить фильм"));
    }

    public Film getFilm(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film updateFilm(Long id, Film film) {
        Film existing = filmStorage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));

        // Обновляем каждое поле только если оно передано, иначе оставляем старое значение
        if (film.getName() != null && !film.getName().isBlank()) {
            existing.setName(film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            validateDescription(film.getDescription());
            existing.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            validateReleaseDate(film.getReleaseDate());
            existing.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null) {
            validateDuration(film.getDuration());
            existing.setDuration(film.getDuration());
        }
        if (film.getDuration() != null) {
            existing.setDuration(film.getDuration());
        }

        return filmStorage.update(id, existing);
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration != null && duration <= 0) {
            throw new ValidationException("Продолжительность должна быть больше нуля");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(CINEMA_BIRTH_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    public void deleteFilm(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID фильма не может быть пустым");
        }
        Film deleted = filmStorage.delete(id);
        if (deleted == null) {
            throw new FilmNotFoundException("Фильм с id " + id + " не найден");
        }
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }
}
