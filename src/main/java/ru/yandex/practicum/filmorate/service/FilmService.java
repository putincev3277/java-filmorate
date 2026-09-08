package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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
        filmStorage.delete(id);
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    // Привет, Ирек! в этот раз поменьше))
    public void addLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count <= 0) {
            return List.of();
        }
        return filmStorage.getMostPopularFilms(count);
    }

    private void getFilmOrThrow(Long id) {
        filmStorage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));
    }

    private void getUserOrThrow(Long id) {
        userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }
}
