package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    private final GenreDbStorage genreStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaDbStorage;

    /**
     * Единая точка валидации бизнес-правил для фильма.
     * Вызывается и при создании, и при обновлении.
     */
    private void validateFilm(Film film) {
        validateReleaseDate(film.getReleaseDate());
        validateDescription(film.getDescription());
        validateDuration(film.getDuration());
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
    }

    // --- Конвертация: Mpa (DTO) -> MpaRating (для БД) ---
    private void applyMpaConversion(Film film) {
        if (film.getMpa() != null && film.getMpa().id() != null) {
            Long mpaId = film.getMpa().id();
            try {
                film.setMpaRating(MpaRating.valueOfId(mpaId));
            } catch (IllegalArgumentException e) {
                throw new MpaNotFoundException("MPA с id=" + mpaId + " не найден");
            }
        } else {
            film.setMpaRating(null);
        }
    }

    private FilmResponse toFilmResponse(Film film) {
        Long mpaId = (film.getMpaRating() != null) ? film.getMpaRating().getId() : null;
        Mpa mpaFromDb = null;
        if (mpaId != null) {
            try {
                mpaFromDb = mpaDbStorage.findById(mpaId);
            } catch (NotFoundException e) {
                // Если справочник повреждён — отдаём null, чтобы не ломать ответ
                mpaFromDb = null;
            }
        }

        List<Genre> genres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genres = film.getGenres().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(id -> {
                        try {
                            return genreStorage.findById(id);
                        } catch (NotFoundException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return new FilmResponse(
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaFromDb,
                genres,
                new ArrayList<>(film.getLikes())
        );
    }

    public FilmResponse createFilm(Film film) {
        // Сначала валидируем всё целиком
        validateFilm(film);

        // Конвертируем Mpa -> MpaRating ПЕРЕД сохранением
        applyMpaConversion(film);

        Film saved = filmStorage.add(film)
                .orElseThrow(() -> new NotFoundException("Не удалось сохранить фильм"));

        return toFilmResponse(saved);
    }

    public FilmResponse getFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));
        return toFilmResponse(film);
    }

    /**
     * Обновление фильма: сначала валидируем входящие данные, затем аккуратно копируем только не-null поля.
     */
    public Film updateFilm(Long id, Film film) {
        Film existing = filmStorage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));

        // Валидируем только те поля, которые пришли в запросе.
        // Для этого создадим временный объект с нужными полями — это самый чистый способ.
        Film toValidate = new Film();
        toValidate.setName(film.getName());
        toValidate.setDescription(film.getDescription());
        toValidate.setReleaseDate(film.getReleaseDate());
        toValidate.setDuration(film.getDuration());
        toValidate.setMpa(film.getMpa());
        toValidate.setGenres(film.getGenres());

        validateFilm(toValidate);

        // Применяем изменения к существующему объекту
        if (film.getName() != null && !film.getName().isBlank()) {
            existing.setName(film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            existing.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            existing.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null) {
            existing.setDuration(film.getDuration());
        }

        // MPA конвертируем и ставим в existing
        if (film.getMpa() != null) {
            applyMpaConversion(existing); // этот метод использует film.getMpa() из контекста, но лучше передать явно
            // Чтобы было прозрачнее, сделаем так:
            try {
                existing.setMpaRating(MpaRating.valueOfId(film.getMpa().id()));
            } catch (IllegalArgumentException e) {
                throw new MpaNotFoundException("MPA с id=" + film.getMpa().id() + " не найден");
            }
        }

        // Жанры тоже можно обновлять, если они пришли
        if (film.getGenres() != null) {
            validateGenres(film.getGenres());
            existing.setGenres(new HashSet<>(film.getGenres()));
        }

        return filmStorage.update(id, existing);
    }

    public FilmResponse updateFilmResponse(Long id, Film film) {
        Film updated = updateFilm(id, film);
        return toFilmResponse(updated);
    }

    public List<FilmResponse> getAllFilmResponses() {
        return filmStorage.findAll().stream()
                .map(this::toFilmResponse)
                .collect(Collectors.toList());
    }

    public List<FilmResponse> getMostPopularFilmsResponse(int count) {
        if (count <= 0) {
            return List.of();
        }
        return filmStorage.getMostPopularFilms(count).stream()
                .map(this::toFilmResponse)
                .collect(Collectors.toList());
    }

    private void validateGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        for (Long id : genreIds) {
            if (id == null) {
                throw new NotFoundException("Жанр не указан (null ID)");
            }
            Genre foundGenre = genreStorage.findById(id);
            if (foundGenre == null) {
                throw new NotFoundException("Жанр с id=" + id + " не найден");
            }
        }
    }

    private void validateMpa(Mpa mpa) {
        if (mpa == null) {
            return;
        }
        Long id = mpa.id();
        if (id == null) {
            return;
        }
        Mpa found = mpaDbStorage.findById(id);
        if (found == null) {
            throw new MpaNotFoundException("MPA с id " + id + " не найден");
        }
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
        if (releaseDate == null) {
            // Если дата обязательна — валидацию лучше делать через аннотации.
            // Здесь проверяем только диапазон, если дата есть.
            return;
        }
        if (releaseDate.isBefore(CINEMA_BIRTH_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    public void deleteFilm(Long id) {
        filmStorage.delete(id);
    }

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
