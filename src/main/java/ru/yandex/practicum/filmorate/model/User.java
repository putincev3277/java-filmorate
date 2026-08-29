package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;  // может быть пустым

    @NotNull(message = "Дата рождения не может быть пустой")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
