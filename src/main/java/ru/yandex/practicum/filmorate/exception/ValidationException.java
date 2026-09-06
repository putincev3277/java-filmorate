package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Slf4j
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
        log.error("Ошибка валидации: {}", message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        log.error("Ошибка валидации: {}", message, cause);
    }
}
