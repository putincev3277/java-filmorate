package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
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
    public Film createFilm(@Valid @RequestBody Film film) throws ValidationException {
        try {
            log.info("Создается новый фильм: {}", film);
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
            }
            return filmService.createFilm(film); // !! ИЗМЕНЕНО !!
        } catch (Exception e) {
            log.error("Ошибка при создании фильма: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable Long id, @RequestBody Film film) {
        try {
            log.info("Обновление фильма с ID: {}", id);
            return filmService.updateFilm(id, film);
        } catch (Exception e) {
            log.error("Ошибка при обновлении фильма: {}", e.getMessage(), e);
            throw new RuntimeException("Произошла ошибка при обновлении фильма", e);
        }
    }

    @PutMapping
    public Film updateFilmWithoutId(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Для обновления через PUT /films поле id обязательно в теле запроса");
        }
        log.info("Обновление фильма (через PUT /films) с ID из тела: {}", film.getId());
        return prepareAndUpdate(film);
    }

    // Общая логика подготовки и обновления — чтобы не дублировать проверки и вызовы
    private Film prepareAndUpdate(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID фильма обязателен для обновления");
        }

        // Если нужно добавить какие-то общие проверки перед обновлением — делай тут
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        Film updatedFilm = filmService.updateFilm(film.getId(), film);
        log.info("Фильм успешно обновлен: {}", updatedFilm);
        return updatedFilm;
    }


    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms(); // !! ИЗМЕНЕНО !!
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilm(@PathVariable Long id) {
        log.info("Получение фильма с ID: {}", id);
        return filmService.getFilm(id); // если нет фильма — сервис выбросит FilmNotFoundException
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long id) {
        try {
            log.info("Удаление фильма с ID: {}", id);
            filmService.deleteFilm(id); // !! ИЗМЕНЕНО !!
        } catch (FilmNotFoundException e) {
            log.warn("Фильм с ID {} не найден", id);
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при удалении фильма: {}", e.getMessage());
            throw e;
        }
    }
}

