package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MpaController {

    private final MpaDbStorage mpaDbStorage;

    @GetMapping("/mpa")
    public List<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }

    @GetMapping("/mpa/{id}")
    public Mpa findById(@PathVariable Long id) {

        Mpa mpa = mpaDbStorage.findById(id);

        if (mpa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "MPA с id " + id + " не найден");
        }

        return mpa;
    }
}
