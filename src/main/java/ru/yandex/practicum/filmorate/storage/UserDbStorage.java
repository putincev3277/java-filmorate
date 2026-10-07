package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("userDbStorage")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        user.setBirthday(birthday != null ? birthday.toLocalDate() : null);
        return user;
    };

    @Override
    public User create(User user) {

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(((Number) keyHolder.getKey()).longValue());
        log.info("Создан пользователь id={} login={}", user.getId(), user.getLogin());
        return user;
    }


    @Override
    public User update(Long id, User user) {

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                id);
        user.setId(id);
        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, userMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> result = jdbcTemplate.query(sql, userMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByLogin(String login) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE login = ?", Integer.class, login);
        return count != null && count > 0;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        // Односторонняя дружба: заявка пишется только в направлении user -> friend
        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, ?)",
                userId, friendId, FriendshipStatus.PENDING.name());
        // Если второй пользователь уже отправил заявку первому — обе стороны CONFIRMED
        Integer reverse = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, friendId, userId);
        if (reverse != null && reverse > 0) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.CONFIRMED.name(), userId, friendId);
            jdbcTemplate.update(
                    "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.CONFIRMED.name(), friendId, userId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // Одностороннее удаление: убираем только СВОЮ запись, запись друга не трогаем
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId);
    }

    @Override
    public Set<Long> getFriendsIds(Long userId) {
        // В список друзей попадают только те, кого пользователь добавил сам
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }


    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> friends1 = getFriendsIds(userId1);
        Set<Long> friends2 = getFriendsIds(userId2);
        friends1.retainAll(friends2);
        if (friends1.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(friends1.size(), "?"));
        String sql = "SELECT * FROM users WHERE id IN (" + placeholders + ") ORDER BY id";
        return jdbcTemplate.query(sql, userMapper, friends1.toArray());
    }

    private record FriendshipRow(Long userId, Long friendId, FriendshipStatus status) {}

    private Optional<FriendshipRow> findFriendship(Long userId, Long friendId) {
        return jdbcTemplate.query(
                        "SELECT user_id, friend_id, status FROM friendships WHERE user_id = ? AND friend_id = ?",
                        (rs, n) -> new FriendshipRow(
                                rs.getLong("user_id"),
                                rs.getLong("friend_id"),
                                FriendshipStatus.valueOf(rs.getString("status"))),
                        userId, friendId)
                .stream().findFirst();
    }
}
