package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FilmDto {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
}