package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public User create(User user) {
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    public User update(Long id, User user) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException("Пользователь с id " + id + " не найден");
        }
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(u -> email.equals(u.getEmail()));
    }

    public boolean existsByLogin(String login) {
        return users.values().stream()
                .anyMatch(u -> login.equals(u.getLogin()));
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (friend == null) {
            throw new UserNotFoundException("Пользователь с id " + friendId + " не найден");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (friend == null) {
            throw new UserNotFoundException("Пользователь с id " + friendId + " не найден");
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public Set<Long> getFriendsIds(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }
        return new HashSet<>(user.getFriends());
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User u1 = users.get(userId1);
        User u2 = users.get(userId2);
        if (u1 == null) {
            throw new UserNotFoundException("Пользователь с id " + userId1 + " не найден");
        }
        if (u2 == null) {
            throw new UserNotFoundException("Пользователь с id " + userId2 + " не найден");
        }

        Set<Long> f1 = u1.getFriends();
        Set<Long> f2 = u2.getFriends();

        return f1.stream()
                .filter(f2::contains)
                .map(users::get)
                .collect(Collectors.toList());
    }
}
