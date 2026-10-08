package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> findAll();

    Optional<Genre> findById(Long id);

    List<Genre> findAllByIds(List<Long> ids);

    Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds);

}
