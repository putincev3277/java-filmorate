package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreDbStorage genreDbStorage;

    @Override
    public List<Genre> findAll() {
        return genreDbStorage.findAll();
    }

    @Override
    public Genre findById(Long id) {
        return genreDbStorage.findById(id);
    }
}
