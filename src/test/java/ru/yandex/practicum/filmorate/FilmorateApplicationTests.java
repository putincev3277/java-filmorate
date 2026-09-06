package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        // Проверка запуска контекста
    }

    @Test
    void testCreateUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setLogin("testUser");
        userDto.setName("Test Name");
        userDto.setEmail("test@example.com");
        userDto.setBirthday(LocalDate.of(1990, 1, 1));

        String userJson = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("login", is("testUser")))
                .andExpect(jsonPath("name", is("Test Name")))
                .andExpect(jsonPath("email", is("test@example.com")));
    }

    @Test
    void testUpdateUser() throws Exception {
        // Создаём пользователя
        UserDto createDto = new UserDto();
        createDto.setLogin("testUser");
        createDto.setName("Test Name");
        createDto.setEmail("test@example.com");
        createDto.setBirthday(LocalDate.of(1990, 1, 1));
        String createJson = objectMapper.writeValueAsString(createDto);

        MvcResult createResult = mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        // Обновляем пользователя
        UserDto updateDto = new UserDto();
        updateDto.setLogin("updatedUser");
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");
        updateDto.setBirthday(LocalDate.of(1990, 1, 1));
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("login", is("updatedUser")))
                .andExpect(jsonPath("name", is("Updated Name")));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("login", is(notNullValue())));
    }

    @Test
    void testUserNotFound() throws Exception {
        mockMvc.perform(get("/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateFilm() throws Exception {
        FilmDto filmDto = new FilmDto();
        filmDto.setName("Тест фильм");
        filmDto.setDescription("Описание фильма");
        filmDto.setReleaseDate(LocalDate.of(2026, 1, 1));
        filmDto.setDuration(120);
        String filmJson = objectMapper.writeValueAsString(filmDto);

        mockMvc.perform(post("/films")
                        .contentType(APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("name", is("Тест фильм")))
                .andExpect(jsonPath("description", is("Описание фильма")))
                .andExpect(jsonPath("releaseDate", is("2026-01-01")))
                .andExpect(jsonPath("duration", is(120)));
    }

    @Test
    void testUpdateFilm() throws Exception {
        FilmDto createDto = new FilmDto();
        createDto.setName("Оригинальный фильм");
        createDto.setDescription("Исходное описание");
        createDto.setReleaseDate(LocalDate.of(2026, 1, 1));
        createDto.setDuration(120);
        String createJson = objectMapper.writeValueAsString(createDto);

        MvcResult createResult = mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        assertThat(responseBody).isNotEmpty();

        Film createdFilm = objectMapper.readValue(responseBody, Film.class);
        Long filmId = createdFilm.getId();
        assertThat(filmId).isNotNull();

        FilmDto updateDto = new FilmDto();
        updateDto.setName("Обновленный фильм");
        updateDto.setDescription("Новое описание");
        updateDto.setReleaseDate(LocalDate.of(2026, 2, 1));
        updateDto.setDuration(150);
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(
                        put("/films/{id}", filmId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(is("Обновленный фильм")))
                .andExpect(jsonPath("$.description").value(is("Новое описание")))
                .andExpect(jsonPath("$.releaseDate").value(is("2026-02-01")))
                .andExpect(jsonPath("$.duration").value(is(150)));
    }

    @Test
    void testDeleteFilm() throws Exception {
        FilmDto createDto = new FilmDto();
        createDto.setName("Тест фильм");
        createDto.setDescription("Описание фильма");
        createDto.setReleaseDate(LocalDate.of(2026, 1, 1));
        createDto.setDuration(120);
        String createJson = objectMapper.writeValueAsString(createDto);

        MvcResult createResult = mockMvc.perform(
                        post("/films")
                                .contentType(APPLICATION_JSON)
                                .content(createJson)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        assertThat(responseBody).isNotEmpty();

        Film createdFilm = objectMapper.readValue(responseBody, Film.class);
        Long filmId = createdFilm.getId();
        assertThat(filmId).isNotNull();

        mockMvc.perform(delete("/films/{id}", filmId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/" + filmId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFilmNotFound() throws Exception {
        mockMvc.perform(get("/films/999999"))
                .andExpect(status().isNotFound());
    }
}
