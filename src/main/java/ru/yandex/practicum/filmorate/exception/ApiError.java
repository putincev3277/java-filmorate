package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Slf4j
public class ApiError {
    // Геттеры
    private final int status;
    private final String message;
    private final String details;
    private final LocalDateTime timestamp;
    private List<String> errors;

    public ApiError(int status, String message, String details) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
        log.debug("Создан ApiError: {}", this);
    }

    public ApiError(int status, String message, String details, List<String> errors) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
        log.debug("Создан ApiError с ошибками: {}", this);
    }

}

