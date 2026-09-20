package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface UserStorage {

    User create(User user);

    User update(Long id, User user);

    List<User> getAll();

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriendsIds(Long userId);

    List<User> getCommonFriends(Long userId1, Long userId2);

}