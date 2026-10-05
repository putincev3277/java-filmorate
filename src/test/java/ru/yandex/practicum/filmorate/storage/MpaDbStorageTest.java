package ru.yandex.practicum.filmorate.storage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.Mpa;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate.storage")
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void shouldFindAllMpa() {
        Assertions.assertThat(mpaStorage.findAll()).hasSize(5);
    }

    @Test
    void shouldFindMpaById() {
        Mpa mpa = mpaStorage.findById(1L);
        Assertions.assertThat(mpa).isNotNull();
        Assertions.assertThat(mpa.id()).isEqualTo(1L);
        Assertions.assertThat(mpa.name()).isEqualTo("G");
    }

    @Test
    void shouldReturnNullForUnknownMpa() {
        Assertions.assertThat(mpaStorage.findById(999L)).isNull();
    }
}
