package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@Qualifier("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    // Один общий RowMapper — его можно использовать везде
    private static final RowMapper<Mpa> ROW_MAPPER = (rs, rowNum) ->
            new Mpa(
                    rs.getLong("id"),
                    rs.getString("name")
            );

    public List<Mpa> findAll() {
        // Проверь название таблицы: в твоём коде было mpa, а в findById — mpa
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", ROW_MAPPER);
    }

    public Mpa findById(Long id) {
        List<Mpa> results = jdbcTemplate.query(
                "SELECT * FROM mpa WHERE id = ?",
                ROW_MAPPER,
                id
        );
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
