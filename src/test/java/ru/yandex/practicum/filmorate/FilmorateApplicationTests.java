package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import ru.yandex.practicum.filmorate.model.Film;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired  // Добавляем автовайринг ObjectMapper
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testCreateUser() throws Exception {
        String userJson = """
                {
                    "login": "testUser",
                    "name": "Test Name",
                    "email": "test@example.com",
                    "birthday": "1990-01-01"
                }
                """;

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
        // Сначала создаем пользователя
        String createUserJson = """
                {
                    "login": "testUser",
                    "name": "Test Name",
                    "email": "test@example.com",
                    "birthday": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(createUserJson))
                .andExpect(status().isCreated());

        // Теперь обновляем созданного пользователя
        String updateJson = """
                {
                    "login": "updatedUser",
                    "name": "Updated Name",
                    "email": "updated@example.com",
                    "birthday": "1990-01-01"
                }
                """;

        mockMvc.perform(put("/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(updateJson))
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
        String filmJson = """
                {
                    "name": "Тест фильм",
                    "description": "Описание фильма",
                    "releaseDate": "2026-01-01",
                    "duration": 120
                }
                """;

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
        String createJson = """
                {
                    "name": "Оригинальный фильм",
                    "description": "Исходное описание",
                    "releaseDate": "2026-01-01",
                    "duration": 120
                }
                """;

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

        String updateJson = """
                {
                    "name": "Обновленный фильм",
                    "description": "Новое описание",
                    "releaseDate": "2026-02-01",
                    "duration": 150
                }
                """;

        mockMvc.perform(
                        put("/films/{id}", filmId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
// ID можно не проверять через jsonPath, если ты доверяешь логике сервиса
                .andExpect(jsonPath("$.name").value(is("Обновленный фильм")))
                .andExpect(jsonPath("$.description").value(is("Новое описание")))
                .andExpect(jsonPath("$.releaseDate").value(is("2026-02-01")))
                .andExpect(jsonPath("$.duration").value(is(150)));
    }


    @Test
    void testDeleteFilm() throws Exception {
        String createJson = """
                {
                    "name": "Тест фильм",
                    "description": "Описание фильма",
                    "releaseDate": "2026-01-01",
                    "duration": 120
                }
                """;

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

        // Опционально: проверить, что фильм действительно удалён
        mockMvc.perform(get("/films/" + filmId))
                .andExpect(status().isNotFound());
    }


    @Test
    void testFilmNotFound() throws Exception {
        mockMvc.perform(get("/films/999999"))
                .andExpect(status().isNotFound());
    }
}
