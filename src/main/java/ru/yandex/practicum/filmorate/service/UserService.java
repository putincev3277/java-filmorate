package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserStorage storage;
    private final AtomicLong idCounter = new AtomicLong(0);

    public User createUser(User user) {
        // Генерируем уникальный ID
        user.setId(idCounter.incrementAndGet());

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

    public User updateUser(Long id, User user) {
        // Проверяем существование пользователя
        User existingUser = storage.findById(id);
        if (existingUser == null) {
            throw new UserNotFoundException("Пользователь не найден");
        }

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
        User user = storage.findById(id);
        if (user == null) {
            throw new UserNotFoundException("Пользователь не найден");
        }
        return user;
    }
}
