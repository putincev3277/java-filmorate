package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создается новый пользователь: {}", user);
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        log.info("Обновление пользователя с ID: {}", id);
        user.setId(id);
        return userService.updateUser(id, user);
    }

    @PutMapping
    public User updateUserWithoutId(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException(
                    "Для обновления через PUT /users поле id обязательно в теле запроса"
            );
        }
        log.info("Обновление пользователя (через PUT /users) с ID из тела: {}", user.getId());
        return userService.updateUser(user.getId(), user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userService.getUser(id);
    }

    /**
     * Добавить друга: PUT /users/{id}/friends/{friendId}
     * По условию: дружба симметричная (оба становятся друзьями друг другу)
     */
    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    /**
     * Удалить из друзей: DELETE /users/{id}/friends/{friendId}
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Пользователь {} удаляет из друзей пользователя {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Получение списка друзей для пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Поиск общих друзей для пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
