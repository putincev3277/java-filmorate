package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Film.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotNull
    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    @NotNull
    @PastOrPresent(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность должна быть положительным числом")
    private Integer duration;
}

