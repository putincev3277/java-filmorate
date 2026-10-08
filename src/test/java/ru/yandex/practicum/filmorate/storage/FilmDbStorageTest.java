package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        // Очищаем в порядке «от зависимых к независимым»
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM friendships");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM mpa");
        jdbcTemplate.execute("DELETE FROM genres");

        // Справочники с фиксированными ID
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (1, 'G', NULL)");
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (3, 'PG-13', NULL)");
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (4, 'R', NULL)");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (3, 'Фантастика')");
    }

    private User createUser(String prefix) {
        String suffix = String.valueOf(System.nanoTime());
        return userStorage.create(User.builder()
                .email(prefix + "_" + suffix + "@example.com")
                .login(prefix + "_" + suffix)
                .name(prefix + " User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
    }

    // PG-13 в курсе обычно имеет id=3; если у тебя другая нумерация — поправь 3L на нужный
    private Film createFilm(String name) {
        return Film.builder()
                .name(name)
                .description(name + " описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpaRating(MpaRating.valueOfId(3L))
                .build();
    }

    @Test
    void shouldAddAndFindFilmById() {
        Film saved = filmStorage.add(createFilm("Титаник")).orElseThrow();

        assertNotNull(saved.getId());

        Film fromDb = filmStorage.findById(saved.getId()).orElseThrow();
        assertEquals("Титаник", fromDb.getName());
        assertEquals(LocalDate.of(2000, 1, 1), fromDb.getReleaseDate());
        assertEquals(120, fromDb.getDuration());
    }

    @Test
    void shouldSaveAndReadGenres() {
        Film film = createFilm("С жанрами");
        film.setGenres(new HashSet<>(Set.of(1L, 2L)));

        Film saved = filmStorage.add(film).orElseThrow();

        List<Long> genreIds = filmStorage.getGenreIds(saved.getId());
        assertEquals(Set.of(1L, 2L), new HashSet<>(genreIds));
    }

    @Test
    void shouldReturnEmptyForUnknownFilm() {
        assertTrue(filmStorage.findById(9999L).isEmpty());
    }

    @Test
    void shouldUpdateFilmAndReplaceGenres() {
        Film film = createFilm("Старое название");
        film.setGenres(new HashSet<>(Set.of(1L)));
        Film saved = filmStorage.add(film).orElseThrow();

        saved.setName("Новое название");
        saved.setDescription("Новое описание");
        saved.setReleaseDate(LocalDate.of(2001, 2, 2));
        saved.setDuration(150);
        saved.setGenres(new HashSet<>(Set.of(2L, 3L)));

        filmStorage.update(saved.getId(), saved);

        Film fromDb = filmStorage.findById(saved.getId()).orElseThrow();
        assertEquals("Новое название", fromDb.getName());
        assertEquals("Новое описание", fromDb.getDescription());
        assertEquals(150, fromDb.getDuration());
        assertEquals(Set.of(2L, 3L), new HashSet<>(filmStorage.getGenreIds(saved.getId())));
    }

    @Test
    void shouldDeleteFilm() {
        Film saved = filmStorage.add(createFilm("На удаление")).orElseThrow();

        filmStorage.delete(saved.getId());

        assertTrue(filmStorage.findById(saved.getId()).isEmpty());
        assertTrue(filmStorage.getGenreIds(saved.getId()).isEmpty());
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.add(createFilm("Фильм 1"));
        filmStorage.add(createFilm("Фильм 2"));

        List<Film> all = filmStorage.findAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(f -> "Фильм 1".equals(f.getName())));
        assertTrue(all.stream().anyMatch(f -> "Фильм 2".equals(f.getName())));
    }

    @Test
    void shouldAddLikeWithoutDuplicatesAndRemoveIt() {
        User user = createUser("like_user");
        Film saved = filmStorage.add(createFilm("С лайком")).orElseThrow();

        filmStorage.addLike(saved.getId(), user.getId());
        filmStorage.addLike(saved.getId(), user.getId()); // повторный лайк не должен создать дубль

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());
        assertEquals(1, likesCount);

        filmStorage.removeLike(saved.getId(), user.getId());
        Integer likesAfterRemove = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());
        assertEquals(0, likesAfterRemove);
    }

    @Test
    void shouldOrderFilmsByLikesCount() {
        User u1 = createUser("u1");
        User u2 = createUser("u2");

        Film popular = filmStorage.add(createFilm("Популярный")).orElseThrow();
        Film lessPopular = filmStorage.add(createFilm("Менее популярный")).orElseThrow();

        filmStorage.addLike(popular.getId(), u1.getId());
        filmStorage.addLike(popular.getId(), u2.getId());
        filmStorage.addLike(lessPopular.getId(), u1.getId());

        List<Film> top = filmStorage.getMostPopularFilms(2);
        assertEquals(2, top.size());
        assertEquals("Популярный", top.get(0).getName());
        assertEquals("Менее популярный", top.get(1).getName());
    }

    @Test
    void shouldCascadeDeleteLikesWithFilm() {
        User user = createUser("cascade_user");
        Film saved = filmStorage.add(createFilm("Каскад")).orElseThrow();
        filmStorage.addLike(saved.getId(), user.getId());

        filmStorage.delete(saved.getId());

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());
        assertEquals(0, likesCount);
    }
}
