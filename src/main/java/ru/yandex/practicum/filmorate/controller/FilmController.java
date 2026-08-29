package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Создается новый фильм: {}", film);
        return filmService.createFilm(film);
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable Long id, @RequestBody Film film) {
        log.info("Обновление фильма с ID: {}", id);
        return filmService.updateFilm(id, film);
    }

    @PutMapping
    public Film updateFilmWithoutId(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Для обновления через PUT /films поле id обязательно в теле запроса");
        }
        log.info("Обновление фильма (через PUT /films) с ID из тела: {}", film.getId());
        return filmService.updateFilm(film.getId(), film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Вызван метод getAllFilms");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilm(@PathVariable Long id) {
        log.info("Получение фильма с ID: {}", id);
        return filmService.getFilm(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long id) {
        log.info("Удаление фильма с ID: {}", id);
        filmService.deleteFilm(id);
    }
}
