package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage; // Важно: внедряем интерфейс!

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreStorage genreStorage; // Было GenreDbStorage — стало GenreStorage

    @Override
    public List<Genre> getAll() {
        return genreStorage.findAll();
    }

    @Override
    public Optional<Genre> getById(Long id) {
        return genreStorage.findById(id);
    }

    @Override
    public List<Genre> getAllByIds(List<Long> ids) {
        return genreStorage.findAllByIds(ids);
    }
}
