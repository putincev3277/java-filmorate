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
    public User createUser(@Valid @RequestBody User user) throws ValidationException {
        try {
            log.info("Создается новый пользователь: {}", user);

            // Проверка и установка имени
            if (user.getName() == null || user.getName().isEmpty()) {
                user.setName(user.getLogin());
            }

            User createdUser = userService.createUser(user);
            log.info("Пользователь успешно создан: {}", createdUser);
            return createdUser;
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            throw new ValidationException("Ошибка при создании пользователя", e);
        }
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        log.info("Обновление пользователя с ID: {}", id);

        user.setId(id);

        // Подстановка имени, если оно не передано
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

        User updatedUser = userService.updateUser(id, user);
        log.info("Пользователь успешно обновлен: {}", updatedUser);
        return updatedUser;
    }

    // Новый PUT /users (для тестов, где ID в теле)
    @PutMapping
    public User updateUserWithoutId(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException(
                    "Для обновления через PUT /users поле id обязательно в теле запроса"
            );
        }
        log.info("Обновление пользователя (через PUT /users) с ID из тела: {}", user.getId());
        return prepareAndUpdate(user);
    }

    // Общая логика подготовки и обновления (чтобы не дублировать код)
    private User prepareAndUpdate(User user) {
        if (user.getId() == null) {
            // Это может понадобиться, если логика вызывается откуда-то ещё
            throw new ValidationException("ID пользователя обязателен для обновления");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        User updatedUser = userService.updateUser(user.getId(), user);
        log.info("Пользователь успешно обновлен: {}", updatedUser);
        return updatedUser;
    }


    @GetMapping
    public List<User> getAllUsers() {
        try {
            log.info("Получение списка всех пользователей");
            List<User> users = userService.getAllUsers();
            log.info("Получено {} пользователей", users.size());
            return users;
        } catch (Exception e) {
            log.error("Ошибка при получении списка пользователей: {}", e.getMessage());
            throw new ValidationException("Ошибка при получении списка пользователей", e);
        }
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userService.getUser(id); // если сервис выбросит UserNotFoundException — будет 404
    }
}

