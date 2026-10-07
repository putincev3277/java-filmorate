package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> findAll();
    Optional<Genre> findById(Long id);
    // Сюда позже добавишь findAllByIds(List<Long> ids)
    // List<Genre> findAllByIds(List<Long> ids);
}
