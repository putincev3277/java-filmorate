package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MpaDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MpaDbStorage mpaStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM films");
    }

    @Test
    void shouldFindAllMpaOrderedById() {
        List<Mpa> all = mpaStorage.findAll();

        assertTrue(!all.isEmpty(), "В базе должны быть записи MPA из data.sql");

        long prevId = -1;
        for (Mpa mpa : all) {
            // Для record используем mpa.id(), а не mpa.getId()
            assertTrue(mpa.id() > prevId, "MPA должны быть отсортированы по возрастанию id");
            prevId = mpa.id();
        }
    }

    @Test
    void shouldFindMpaById() {
        List<Mpa> all = mpaStorage.findAll();
        if (all.isEmpty()) {
            fail("Нет записей MPA в базе — проверь data.sql или миграции");
        }
        Mpa first = all.get(0);

        Optional<Mpa> found = mpaStorage.findById(first.id());

        assertTrue(found.isPresent());
        assertEquals(first.id(), found.get().id());
        assertEquals(first.name(), found.get().name());
    }

    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        Optional<Mpa> notFound = mpaStorage.findById(-999L);
        assertTrue(notFound.isEmpty());
    }

    @Test
    void shouldFindMpaByIds() {
        List<Mpa> all = mpaStorage.findAll();
        if (all.size() < 2) {
            return; // Недостаточно данных для теста
        }

        List<Long> ids = List.of(all.get(0).id(), all.get(1).id());
        List<Mpa> result = mpaStorage.findAllByIds(ids);

        assertEquals(2, result.size());
        assertTrue(result.contains(all.get(0)));
        assertTrue(result.contains(all.get(1)));
    }

    @Test
    void shouldReturnEmptyListForEmptyOrNullIds() {
        assertTrue(mpaStorage.findAllByIds(List.of()).isEmpty());
        assertTrue(mpaStorage.findAllByIds(null).isEmpty());
    }
}
