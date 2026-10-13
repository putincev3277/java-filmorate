package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.yandex.practicum.filmorate.deserializer.GenreDeserializer;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    // Проверяет «не в будущем», но не «не раньше 1895». Границу 1895 оставляем в сервисе.
    @NotNull
    @PastOrPresent(message = "Дата релиза должна быть не в будущем")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть пустой")
    @Min(value = 1, message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    @Builder.Default
    @JsonDeserialize(using = GenreDeserializer.class)
    private Set<Long> genres = new HashSet<>();

    /**
     * Поле для приёма JSON от клиента/тестов.
     * Только данные: без логики конвертации.
     */
    private Mpa mpa;

    /**
     * Поле для внутренней логики и БД.
     * Заполняется в сервисе через applyMpaConversion.
     */
    private MpaRating mpaRating;

    @Builder.Default
    private Set<Long> likes = new HashSet<>();
}
