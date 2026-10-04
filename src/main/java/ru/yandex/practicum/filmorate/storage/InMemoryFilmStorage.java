package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Optional<Film> add(Film film) {
        long newId = idCounter.getAndIncrement();
        film.setId(newId);
        films.put(film.getId(), film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film update(Long id, Film film) {
        if (!films.containsKey(id)) {
            throw new FilmNotFoundException("Фильм с id " + id + " не найден");
        }
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film delete(Long id) {
        Film film = films.remove(id);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с id " + filmId + " не найден");
        }
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с id " + filmId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
