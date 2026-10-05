package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@Qualifier("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreMapper = (rs, rowNum) ->
            new Genre(rs.getLong("id"), rs.getString("name"));

    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id", genreMapper);
    }

    public Genre findById(Long id) {
        List<Genre> result = jdbcTemplate.query(
                "SELECT * FROM genres WHERE id = ?", genreMapper, id);
        return result.stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}
