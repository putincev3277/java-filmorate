package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    // Создание пользователя
    public User create(User user) {
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    // Обновление пользователя
    public User update(Long id, User user) {
        if (!users.containsKey(id)) {
            return null;
        }
        user.setId(id);
        users.put(id, user);
        return user;
    }


    // Получение всех пользователей
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    // Проверка существования email
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(u -> email.equals(u.getEmail()));
    }

    // Проверка существования login
    public boolean existsByLogin(String login) {
        return users.values().stream()
                .anyMatch(u -> login.equals(u.getLogin()));
    }

    // Поиск пользователя по ID
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

}
