package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GenreDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GenreDbStorage genreStorage;

    @BeforeEach
    void setUp() {
        // Очищаем только зависимые таблицы (в порядке от дочерних к родительским)
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM films");
        // genres можно чистить, если в них нет внешних ключей на другие таблицы
        jdbcTemplate.execute("DELETE FROM genres");

        // Вставляем фиксированные жанры (если они нужны для тестов)
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (3, 'Фантастика')");
    }

    private Long createFilm(String name) {
        // ВАЖНО: НЕ делаем INSERT INTO mpa — MPA уже есть в БД (из data.sql)
        // Просто используем существующий MPA (например, id = 1)
        Long mpaId = jdbcTemplate.queryForObject("SELECT id FROM mpa LIMIT 1", Long.class);
        if (mpaId == null) {
            throw new IllegalStateException("В базе нет записей в таблице mpa. Проверьте data.sql");
        }

        jdbcTemplate.update("""
                INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
                VALUES (?, ?, ?, ?, ?)
                """, name, name + " описание", java.sql.Date.valueOf("2020-01-01"), 120, mpaId);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM films WHERE name = ?", Long.class, name);
    }

    private void linkGenreToFilm(Long filmId, Long genreId) {
        jdbcTemplate.update(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                filmId, genreId);
    }

    @Test
    void shouldFindAllGenresOrderedById() {
        List<Genre> all = genreStorage.findAll();

        assertEquals(3, all.size());
        assertEquals(new Genre(1L, "Комедия"), all.get(0));
        assertEquals(new Genre(2L, "Драма"), all.get(1));
        assertEquals(new Genre(3L, "Фантастика"), all.get(2));
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> opt = genreStorage.findById(2L);
        assertTrue(opt.isPresent());
        Genre genre = opt.orElseThrow();

        assertEquals(2L, genre.id());
        assertEquals("Драма", genre.name());
    }

    @Test
    void shouldReturnEmptyForUnknownGenre() {
        assertTrue(genreStorage.findById(9999L).isEmpty());
    }

    @Test
    void shouldFindGenresByIdsInAnyOrder() {
        List<Genre> genres = genreStorage.findAllByIds(List.of(3L, 1L));

        assertEquals(2, genres.size());
        assertTrue(genres.contains(new Genre(1L, "Комедия")));
        assertTrue(genres.contains(new Genre(3L, "Фантастика")));
    }

    @Test
    void shouldReturnEmptyListForEmptyIds() {
        assertTrue(genreStorage.findAllByIds(List.of()).isEmpty());
        assertTrue(genreStorage.findAllByIds(null).isEmpty());
    }

    @Test
    void shouldThrowWhenSomeGenresNotFound() {
        assertThrows(NotFoundException.class,
                () -> genreStorage.findAllByIds(List.of(1L, 9999L)));
    }

    @Test
    void shouldGroupGenresByFilmIds() {
        Long film1 = createFilm("Фильм 1");
        Long film2 = createFilm("Фильм 2");

        linkGenreToFilm(film1, 1L);
        linkGenreToFilm(film1, 2L);
        linkGenreToFilm(film2, 3L);

        Map<Long, List<Genre>> result = genreStorage.getGenresByFilmIds(List.of(film1, film2));

        assertEquals(2, result.size());
        assertEquals(2, result.get(film1).size());
        assertEquals(1, result.get(film2).size());

        assertTrue(result.get(film1).contains(new Genre(1L, "Комедия")));
        assertTrue(result.get(film1).contains(new Genre(2L, "Драма")));
        assertEquals(new Genre(3L, "Фантастика"), result.get(film2).get(0));
    }

    @Test
    void shouldReturnEmptyMapForEmptyFilmIds() {
        Map<Long, List<Genre>> result = genreStorage.getGenresByFilmIds(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapWhenNoGenresLinked() {
        Long film = createFilm("Без жанров");
        Map<Long, List<Genre>> result = genreStorage.getGenresByFilmIds(List.of(film));

        assertTrue(result.isEmpty() || result.get(film).isEmpty());
    }

    @Test
    void shouldReturnEmptyMapForNullInput() {
        Map<Long, List<Genre>> result = genreStorage.getGenresByFilmIds(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
