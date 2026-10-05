package ru.yandex.practicum.filmorate.storage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Sql("classpath:data.sql")
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate.storage")
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        Assertions.assertThat(genres).hasSize(6);
        Assertions.assertThat(genres)
                .extracting(Genre::name)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер",
                        "Документальный", "Боевик");
    }

    @Test
    void shouldFindGenreById() {
        Genre genre = genreStorage.findById(1L);

        Assertions.assertThat(genre.id()).isEqualTo(1L);
        Assertions.assertThat(genre.name()).isEqualTo("Комедия");
    }

    @Test
    void shouldThrowForUnknownGenre() {
        Assertions.assertThatThrownBy(() -> genreStorage.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }
}
