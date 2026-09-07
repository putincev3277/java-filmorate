package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage storage;

    public User createUser(User user) {
        setDefaultName(user);

        if (storage.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

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

        if (!existingUser.getEmail().equals(user.getEmail()) &&
                storage.existsByEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        if (!existingUser.getLogin().equals(user.getLogin()) &&
                storage.existsByLogin(user.getLogin())) {
            throw new ValidationException("Пользователь с таким логином уже существует");
        }

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

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        // Проверяем, что оба пользователя существуют
        storage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        storage.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + friendId + " не найден"));
        storage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        storage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        storage.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + friendId + " не найден"));
        storage.removeFriend(userId, friendId);
    }


    /**
     * Получить список друзей пользователя (объекты User).
     */
    public List<User> getFriends(Long userId) {
        storage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Set<Long> friendIds = storage.getFriendsIds(userId);
        return friendIds.stream()
                .map(fid -> storage.findById(fid)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + fid + " не найден")))
                .collect(Collectors.toList());
    }

    /**
     * Получить общих друзей двух пользователей.
     */
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        return storage.getCommonFriends(userId1, userId2);
    }
}
