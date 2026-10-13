package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Film> add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpaRating() != null ? film.getMpaRating().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(((Number) keyHolder.getKey()).longValue());
        saveGenres(film);
        log.info("Создан фильм id={} name={}", film.getId(), film.getName());
        return Optional.of(film);
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";

        List<Film> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            Date release = rs.getDate("release_date");
            film.setReleaseDate(release != null ? release.toLocalDate() : null);
            film.setDuration(rs.getInt("duration"));

            // MPA здесь НЕ загружаем. Его загрузит сервис, если нужно.
            // Если в базе хранится ID, а в объекте enum, конвертацию тоже лучше делать в сервисе,
            // либо хранить ID в модели Film, а enum мапить в DTO.
            Long mpaId = rs.getObject("mpa_rating_id", Long.class);
            if (mpaId != null) {
                film.setMpaRating(MpaRating.valueOfId(mpaId));
            }

            return film;
        }, id);

        return result.stream().findFirst();
    }


    @Override
    public Film update(Long id, Film film) {
        String sql = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE id = ?
            """;
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().getId() : null,
                id);

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        film.setId(id);

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);
        saveGenres(film);
        return film;
    }

    @Override
    public Film delete(Long id) {
        Optional<Film> film = findById(id);
        if (film.isPresent()) {
            jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
            return film.get();
        }
        throw new NotFoundException("Фильм с id=" + id + " не найден");
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films ORDER BY id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            Date release = rs.getDate("release_date");
            film.setReleaseDate(release != null ? release.toLocalDate() : null);
            film.setDuration(rs.getInt("duration"));

            Long mpaId = rs.getObject("mpa_rating_id", Long.class);
            if (mpaId != null) {
                film.setMpaRating(MpaRating.valueOfId(mpaId));
            }

            return film;
        });
    }


    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
        log.debug("Лайк: filmId={}, userId={}", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        // JOIN только с likes для подсчёта. Никаких film_genres и mpa здесь!
        String sql = """
        SELECT f.*, COUNT(l.user_id) AS likes_count
        FROM films f
        LEFT JOIN likes l ON l.film_id = f.id
        GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id
        ORDER BY likes_count DESC, f.id ASC
        FETCH FIRST ? ROWS ONLY
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            Date release = rs.getDate("release_date");
            film.setReleaseDate(release != null ? release.toLocalDate() : null);
            film.setDuration(rs.getInt("duration"));

            Long mpaId = rs.getObject("mpa_rating_id", Long.class);
            if (mpaId != null) {
                film.setMpaRating(MpaRating.valueOfId(mpaId));
            }

            // genres остаются пустыми, их заполнит сервис
            return film;
        }, count);
    }


    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)",
                film.getGenres().stream()
                        .map(genreId -> new Object[]{film.getId(), genreId})
                        .toList());
    }

    @Override
    public List<Long> getGenreIds(Long filmId) {
        return jdbcTemplate.queryForList(
                "SELECT genre_id FROM film_genres WHERE film_id = ? ORDER BY genre_id",
                Long.class, filmId);
    }


}
