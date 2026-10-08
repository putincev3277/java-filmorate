package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreMapper = (rs, rowNum) ->
            new Genre(rs.getLong("id"), rs.getString("name"));

    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id", genreMapper);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> result = jdbcTemplate.query(sql, genreMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public List<Genre> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, name FROM genres WHERE id IN (" + placeholders + ")";

        // ИСПРАВЛЕНО: new Long[0] — это пустой массив Long, всё корректно
        List<Genre> result = jdbcTemplate.query(sql, genreMapper, ids.toArray(new Long[0]));

        if (result.size() != ids.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены по переданным ID");
        }

        return result;
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY fg.film_id";

        RowMapper<GenreRow> rowMapper = new RowMapper<GenreRow>() {
            @Override
            public GenreRow mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
                Long filmId = rs.getLong("film_id");
                Long genreId = rs.getLong("id");
                String genreName = rs.getString("name");
                return new GenreRow(filmId, new Genre(genreId, genreName));
            }
        };

        // ИСПРАВЛЕНО: new Object[0] — корректный пустой массив для параметров
        List<GenreRow> rows = jdbcTemplate.query(sql, rowMapper, filmIds.toArray(new Object[0]));

        return rows.stream()
                .collect(Collectors.groupingBy(
                        GenreRow::getFilmId,
                        Collectors.mapping(GenreRow::getGenre, Collectors.toList())
                ));
    }

    private static class GenreRow {
        private final Long filmId;
        private final Genre genre;

        public GenreRow(Long filmId, Genre genre) {
            this.filmId = filmId;
            this.genre = genre;
        }

        public Long getFilmId() {
            return filmId;
        }

        public Genre getGenre() {
            return genre;
        }
    }
}
