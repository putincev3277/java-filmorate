package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;

@Component
public class InMemoryUserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    // Создание пользователя
    public User create(User user) {
        // Генерируем ID автоматически
        user.setId(idCounter.getAndIncrement());

        // Проверяем уникальность email и login
        if (existsByEmail(user.getEmail())) {
            throw new UserNotFoundException("Пользователь с таким email уже существует");
        }
        if (existsByLogin(user.getLogin())) {
            throw new UserNotFoundException("Пользователь с таким логином уже существует");
        }

        users.put(user.getId(), user);
        return user;
    }

    // Обновление пользователя
    public User update(Long id, User user) {
        if (!users.containsKey(id)) {
            return null;
        }

        // Проверяем уникальность email и login при обновлении
        User existingUser = users.get(id);
        if (!existingUser.getEmail().equals(user.getEmail()) && existsByEmail(user.getEmail())) {
            throw new UserNotFoundException("Пользователь с таким email уже существует");
        }
        if (!existingUser.getLogin().equals(user.getLogin()) && existsByLogin(user.getLogin())) {
            throw new UserNotFoundException("Пользователь с таким логином уже существует");
        }

        user.setId(id); // Убедимся, что ID не изменится
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
    public User findById(Long id) {
        return users.get(id);
    }
}
