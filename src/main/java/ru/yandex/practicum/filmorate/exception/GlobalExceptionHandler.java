package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // <!-- ИЗМЕНЕНО: добавлен импорт -->
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
// <!-- ИЗМЕНЕНО: убран @ResponseStatus с методов, он больше не нужен -->

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    // <!-- ИЗМЕНЕНО: тип возврата ResponseEntity<ApiError> -->
    public ResponseEntity<ApiError> handleValidationException(ValidationException ex) {
        log.warn("Произошла ошибка валидации: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // <!-- ИЗМЕНЕНО: явный статус -->
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation error",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // <!-- ИЗМЕНЕНО: тип возврата ResponseEntity<ApiError> -->
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(String.format("%s: %s", error.getField(), error.getDefaultMessage()));
        }
        log.warn("Ошибки валидации полей: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // <!-- ИЗМЕНЕНО: явный статус -->
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation error",
                        "Validation failed",
                        errors
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    // <!-- ИЗМЕНЕНО: убран @ResponseStatus, тип возврата ResponseEntity<ApiError> -->
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("Пользователь не найден: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // <!-- ИЗМЕНЕНО: явный статус -->
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        "User not found",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(FilmNotFoundException.class)
    // <!-- ИЗМЕНЕНО: убран @ResponseStatus, тип возврата ResponseEntity<ApiError> -->
    public ResponseEntity<ApiError> handleFilmNotFoundException(FilmNotFoundException ex) {
        log.warn("Фильм не найден: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // <!-- ИЗМЕНЕНО: явный статус -->
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        "Film not found",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    // <!-- ИЗМЕНЕНО: тип возврата ResponseEntity<ApiError> -->
    public ResponseEntity<ApiError> handleException(Exception ex) {
        log.error("Произошла непредвиденная ошибка на сервере", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // <!-- ИЗМЕНЕНО: явный статус -->
                .body(new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error",
                        "Произошла непредвиденная ошибка при обработке запроса"
                ));
    }
}
