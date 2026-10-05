package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenreController {

    private final GenreDbStorage genreDbStorage;

    @GetMapping("/genres")
    public List<Genre> findAll() {
        return genreDbStorage.findAll();
    }

    @GetMapping("/genres/{id}")
    public Genre findById(@PathVariable Long id) {
        return genreDbStorage.findById(id);
    }
}
