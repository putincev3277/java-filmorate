package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    // Один общий RowMapper — его можно использовать везде
    private static final RowMapper<Mpa> ROW_MAPPER = (rs, rowNum) ->
            new Mpa(
                    rs.getLong("id"),
                    rs.getString("name")
            );

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", ROW_MAPPER);
    }

    @Override // ② новая сигнатура — как просил Ирек
    public Optional<Mpa> findById(Long id) {
        List<Mpa> results = jdbcTemplate.query(
                "SELECT * FROM mpa WHERE id = ?",
                ROW_MAPPER,
                id
        );
        return results.stream().findFirst(); // ③ пустой список → Optional.empty(), без null
    }

    @Override
    public List<Mpa> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // Формируем строку "?, ?, ?" для количества переданных ID
        String placeholders = ids.stream()
                .map(i -> "?")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String sql = "SELECT * FROM mpa WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.query(sql, ROW_MAPPER, ids.toArray(new Long[]{}));
    }


}
