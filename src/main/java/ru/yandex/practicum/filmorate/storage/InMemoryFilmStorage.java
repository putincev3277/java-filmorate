package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 0L;

    @Override
    public Optional<Film> add(Film film) {
        film.setId(++idCounter);
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
            return null;
        }
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film delete(Long id) {
        return films.remove(id);
    }


    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }
}
