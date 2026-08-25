package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ApiError handleValidationException(ValidationException ex) {
        log.error("Произошла ошибка валидации: {}", ex.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Форматируем как "поле: сообщение" — удобно для чтения и тестов
            errors.add(String.format("%s: %s", error.getField(), error.getDefaultMessage()));
        }
        log.error("Ошибки валидации полей: {}", errors);
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                "Validation failed",
                errors
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFoundException(UserNotFoundException ex) {
        log.error("Пользователь не найден: {}", ex.getMessage());
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "User not found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FilmNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleFilmNotFoundException(FilmNotFoundException ex) {
        log.error("Фильм не найден: {}", ex.getMessage());
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Film not found",
                ex.getMessage()
        );
    }


    @ExceptionHandler(RuntimeException.class)
    public ApiError handleRuntimeException(RuntimeException ex) {
        log.error("Произошла внутренняя ошибка при выполнении операции", ex);
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "Произошла ошибка при обработке запроса"
        );
    }

    @ExceptionHandler(Exception.class)
    public ApiError handleException(Exception ex) {
        log.error("Произошла критическая внутренняя ошибка сервера", ex);
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Critical internal server error",
                "Произошла непредвиденная ошибка на сервере"
        );
    }
}
