package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;
import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(Long id, User user);

    List<User> getAll();

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findById(Long id);

}