package ru.yandex.practicum.filmorate.storage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate.storage")

class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    // Ручной конструктор с @Autowired — самый надёжный вариант для тестов
    @Autowired
    public FilmDbStorageTest(FilmDbStorage filmStorage,
                             UserDbStorage userStorage,
                             JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    private Film createFilm(String name) {
        return Film.builder()
                .name(name)
                .description(name + " описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpaRating(MpaRating.PG_13)
                .genres(new HashSet<>(Set.of(1L, 2L)))
                .build();
    }

    @Test
    void shouldAddAndFindFilmById() {
        Film saved = filmStorage.add(createFilm("Титаник")).orElseThrow();

        Assertions.assertThat(saved.getId()).isNotNull();

        Film fromDb = filmStorage.findById(saved.getId()).orElseThrow();
        Assertions.assertThat(fromDb.getName()).isEqualTo("Титаник");
        Assertions.assertThat(fromDb.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        Assertions.assertThat(fromDb.getDuration()).isEqualTo(120);
        Assertions.assertThat(fromDb.getMpaRating()).isEqualTo(MpaRating.PG_13);
        Assertions.assertThat(fromDb.getGenres()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldReturnEmptyForUnknownFilm() {
        Assertions.assertThat(filmStorage.findById(9999L)).isEmpty();
    }

    @Test
    void shouldUpdateFilm() {
        Film saved = filmStorage.add(createFilm("Старое название")).orElseThrow();

        saved.setName("Новое название");
        saved.setDescription("Новое описание");
        saved.setReleaseDate(LocalDate.of(2001, 2, 2));
        saved.setDuration(150);
        saved.setMpaRating(MpaRating.R);
        saved.setGenres(new HashSet<>(Set.of(3L)));

        filmStorage.update(saved.getId(), saved);

        Film fromDb = filmStorage.findById(saved.getId()).orElseThrow();
        Assertions.assertThat(fromDb.getName()).isEqualTo("Новое название");
        Assertions.assertThat(fromDb.getDescription()).isEqualTo("Новое описание");
        Assertions.assertThat(fromDb.getDuration()).isEqualTo(150);
        Assertions.assertThat(fromDb.getMpaRating()).isEqualTo(MpaRating.R);
        Assertions.assertThat(fromDb.getGenres()).containsExactly(3L);
    }

    @Test
    void shouldDeleteFilm() {
        Film saved = filmStorage.add(createFilm("На удаление")).orElseThrow();

        filmStorage.delete(saved.getId());

        Assertions.assertThat(filmStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.add(createFilm("Фильм 1"));
        filmStorage.add(createFilm("Фильм 2"));

        Assertions.assertThat(filmStorage.findAll())
                .extracting(Film::getName)
                .contains("Фильм 1", "Фильм 2");
    }

    @Test
    void shouldAddLikeWithoutDuplicatesAndRemoveIt() {
        var user = userStorage.create(UserForFilm.create());
        Film saved = filmStorage.add(createFilm("С лайком")).orElseThrow();

        filmStorage.addLike(saved.getId(), user.getId());
        filmStorage.addLike(saved.getId(), user.getId());

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());
        Assertions.assertThat(likesCount).isEqualTo(1);

        filmStorage.removeLike(saved.getId(), user.getId());
        Integer likesAfterRemove = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());
        Assertions.assertThat(likesAfterRemove).isEqualTo(0);
    }

    @Test
    void shouldOrderFilmsByLikesCount() {
        var u1 = userStorage.create(UserForFilm.create());
        var u2 = userStorage.create(UserForFilm.create());

        Film popular = filmStorage.add(createFilm("Популярный")).orElseThrow();
        Film lessPopular = filmStorage.add(createFilm("Менее популярный")).orElseThrow();

        filmStorage.addLike(popular.getId(), u1.getId());
        filmStorage.addLike(popular.getId(), u2.getId());
        filmStorage.addLike(lessPopular.getId(), u1.getId());

        List<Film> top = filmStorage.getMostPopularFilms(2);
        Assertions.assertThat(top).hasSize(2);
        Assertions.assertThat(top.get(0).getName()).isEqualTo("Популярный");
        Assertions.assertThat(top.get(1).getName()).isEqualTo("Менее популярный");
    }

    @Test
    void shouldCascadeDeleteLikesWithFilm() {
        var user = userStorage.create(UserForFilm.create());
        Film saved = filmStorage.add(createFilm("Каскад")).orElseThrow();
        filmStorage.addLike(saved.getId(), user.getId());

        filmStorage.delete(saved.getId());

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());
        Assertions.assertThat(likesCount).isEqualTo(0);
    }

    private static class UserForFilm {
        static ru.yandex.practicum.filmorate.model.User create() {
            // Генерируем уникальный суффикс, чтобы email/login/name никогда не повторялись
            String suffix = System.nanoTime() + "_" + Math.random();
            return ru.yandex.practicum.filmorate.model.User.builder()
                    .email("filmLiker_" + suffix + "@example.com")
                    .login("filmLiker_" + suffix)
                    .name("Film Liker " + suffix)          // обязательно, если name NOT NULL
                    .birthday(LocalDate.of(1990, 1, 1))
                    .build();
        }
    }

}
