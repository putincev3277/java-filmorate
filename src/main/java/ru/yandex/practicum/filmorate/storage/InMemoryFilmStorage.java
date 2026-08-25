package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component

public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 0L;

    @Override
    public Film add(Film film) {
        film.setId(++idCounter);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film findById(Long id) {
        return films.get(id);
    }

    @Override
    public Film update(Long id, Film film) {
        if (!films.containsKey(id)) {
            return null;
        }
        film.setId(id);              // гарантируем ID
        films.put(id, film);         // обновляем в мапе
        return film;                 // возвращаем именно новый объект
    }

    @Override
    public Film delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID фильма не может быть пустым");
        }

        Film deletedFilm = films.remove(id);
        if (deletedFilm == null) {
            throw new FilmNotFoundException("Фильм с ID " + id + " не найден");
        }
        return deletedFilm;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }
}
