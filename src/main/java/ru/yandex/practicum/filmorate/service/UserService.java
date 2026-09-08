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
        User existingUser = getUserOrThrow(id);

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
        return getUserOrThrow(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        storage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        storage.removeFriend(userId, friendId);
    }


    /**
     * Получить список друзей пользователя (объекты User).
     */
    public List<User> getFriends(Long userId) {
        getUserOrThrow(userId);
        Set<Long> friendIds = storage.getFriendsIds(userId);
        return friendIds.stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    /**
     * Получить общих друзей двух пользователей.
     */
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        getUserOrThrow(userId1);
        getUserOrThrow(userId2);
        return storage.getCommonFriends(userId1, userId2);
    }

    private User getUserOrThrow(Long id) {
        return storage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }
}
