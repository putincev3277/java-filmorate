package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage storage;

    public User createUser(User user) {
        // Если имя не указано, используем логин
        setDefaultName(user);

        // Проверка на уникальность email
        if (storage.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Проверка на уникальность login
        if (storage.existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        return storage.create(user);
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    public User updateUser(Long id, User user) {
        setDefaultName(user);
        User existingUser = storage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));

        // Проверяем уникальность email и login
        if (!existingUser.getEmail().equals(user.getEmail()) &&
                storage.existsByEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        if (!existingUser.getLogin().equals(user.getLogin()) &&
                storage.existsByLogin(user.getLogin())) {
            throw new ValidationException("Пользователь с таким логином уже существует");
        }

        // Обновляем данные
        user.setId(id);
        return storage.update(id, user);
    }

    public List<User> getAllUsers() {
        return storage.getAll();
    }

    public User getUser(Long id) {
        return storage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }
}
