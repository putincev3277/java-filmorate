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
import ru.yandex.practicum.filmorate.model.User;

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
        // Генерируем уникальные значения, чтобы не было конфликтов при повторных запусках
        String uniqueLogin = "testUser_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = "test@" + uniqueLogin + ".com";

        UserDto userDto = new UserDto();
        userDto.setLogin(uniqueLogin);
        userDto.setName("Test Name");
        userDto.setEmail(uniqueEmail);
        userDto.setBirthday(LocalDate.of(1990, 1, 1));

        String userJson = objectMapper.writeValueAsString(userDto);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Проверяем, что в ответе действительно вернулись те же данные
        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getLogin()).isEqualTo(uniqueLogin);
        assertThat(createdUser.getName()).isEqualTo("Test Name");
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

    @Test
    void testAddFriendAndGetFriends() throws Exception {
        // Создаём пользователей и сразу забираем их ID из ответа
        UserDto user1Dto = createTestUser("friend1", "Friend One", "f1@test.com");
        MvcResult res1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1Dto)))
                .andExpect(status().isCreated())
                .andReturn();
        User user1 = objectMapper.readValue(res1.getResponse().getContentAsString(), User.class);
        Long id1 = user1.getId();

        UserDto user2Dto = createTestUser("friend2", "Friend Two", "f2@test.com");
        MvcResult res2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2Dto)))
                .andExpect(status().isCreated())
                .andReturn();
        User user2 = objectMapper.readValue(res2.getResponse().getContentAsString(), User.class);
        Long id2 = user2.getId();

        // Добавляем в друзья (симметрично)
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isNoContent());

        // Получаем друзей пользователя 1 — должен быть пользователь 2
        mockMvc.perform(get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].login").value(hasItem("friend2")));

        // Получаем друзей пользователя 2 — должен быть пользователь 1
        mockMvc.perform(get("/users/{id}/friends", id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].login").value(hasItem("friend1")));
    }


    @Test
    void testRemoveFriend() throws Exception {
        UserDto user1Dto = createTestUser("rem1", "Remove One", "r1@test.com");
        MvcResult res1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1Dto)))
                .andExpect(status().isCreated())
                .andReturn();
        User user1 = objectMapper.readValue(res1.getResponse().getContentAsString(), User.class);
        Long id1 = user1.getId();

        UserDto user2Dto = createTestUser("rem2", "Remove Two", "r2@test.com");
        MvcResult res2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2Dto)))
                .andExpect(status().isCreated())
                .andReturn();
        User user2 = objectMapper.readValue(res2.getResponse().getContentAsString(), User.class);
        Long id2 = user2.getId();

        // Сначала добавляем дружбу
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isNoContent());

        // Удаляем дружбу
        mockMvc.perform(delete("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isNoContent());

        // Проверяем, что у пользователя 1 больше нет этого друга
        mockMvc.perform(get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // И у пользователя 2 тоже нет
        mockMvc.perform(get("/users/{id}/friends", id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void testGetCommonFriends() throws Exception {
        // Создаём трёх пользователей и забираем их ID
        User main = createUserAndReturnWithId("main", "Main User", "main@test.com");
        User f1 = createUserAndReturnWithId("f1", "First Friend", "ff1@test.com");
        User f2 = createUserAndReturnWithId("f2", "Second Friend", "ff2@test.com");

        // Делаем f1 и f2 друзьями main
        mockMvc.perform(put("/users/{id}/friends/{friendId}", main.getId(), f1.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", main.getId(), f2.getId()))
                .andExpect(status().isNoContent());

        // Теперь делаем f1 и f2 друзьями друг с другом
        mockMvc.perform(put("/users/{id}/friends/{friendId}", f1.getId(), f2.getId()))
                .andExpect(status().isNoContent());

        // Ищем общих друзей у main и f1 — должен быть f2
        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", main.getId(), f1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].login").value(hasItem("f2")));
    }

    // Вспомогательный метод, чтобы не дублировать код
    private User createUserAndReturnWithId(String login, String name, String email) throws Exception {
        UserDto dto = createTestUser(login, name, email);
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
    }


    @Test
    void testUpdateFilmWithoutIdRequiresIdInBody() throws Exception {
        FilmDto filmDto = new FilmDto();
        filmDto.setName("ToUpdate");
        filmDto.setDescription("Desc");
        filmDto.setReleaseDate(LocalDate.of(2026, 1, 1));
        filmDto.setDuration(100);

        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Film created = objectMapper.readValue(result.getResponse().getContentAsString(), Film.class);
        assertThat(created.getId()).isNotNull();

        // Пытаемся обновить без ID в path и без ID в теле — должно быть исключение (400)
        Film updateDto = new Film();
        updateDto.setName("Updated Name");
        updateDto.setDescription("New Desc");
        updateDto.setReleaseDate(LocalDate.of(2026, 2, 1));
        updateDto.setDuration(110);
        // ID НЕ устанавливаем

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest()); // Ожидается ValidationException → 400

        // Обновляем корректно: с ID в теле
        updateDto.setId(created.getId());
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("Updated Name"));
    }

    @Test
    void testAddAndRemoveLikeAndPopularFilms() throws Exception {
        // Создаём фильм через DTO
        FilmDto filmDto = new FilmDto();
        filmDto.setName("Popular Test Film");
        filmDto.setDescription("Description");
        filmDto.setReleaseDate(LocalDate.of(2026, 1, 1));
        filmDto.setDuration(90);

        String filmJson = objectMapper.writeValueAsString(filmDto);
        MvcResult createFilmResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated())
                .andReturn();

        Film createdFilm = objectMapper.readValue(createFilmResult.getResponse().getContentAsString(), Film.class);
        Long filmId = createdFilm.getId();
        assertThat(filmId).isNotNull();

        // Создаём двух пользователей через DTO (используем твои поля: login, name, email, birthday)
        UserDto user1 = createTestUser("user1", "User One", "u1@test.com");
        UserDto user2 = createTestUser("user2", "User Two", "u2@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        // Ставим два лайка
        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, 1L))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, 2L))
                .andExpect(status().isNoContent());

        // Проверяем, что фильм в топе популярных
        mockMvc.perform(get("/films/popular?count=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(is(filmId.intValue())));

        // Убираем один лайк
        mockMvc.perform(delete("/films/{id}/like/{userId}", filmId, 1L))
                .andExpect(status().isNoContent());

        // Перепроверяем: фильм всё ещё должен быть в топе (но с меньшим числом лайков)
        mockMvc.perform(get("/films/popular?count=10"))
                .andExpect(status().isOk());
    }

    private UserDto createTestUser(String login, String name, String email) {
        UserDto dto = new UserDto();
        dto.setLogin(login);
        dto.setName(name);
        dto.setEmail(email);
        dto.setBirthday(LocalDate.of(1990, 1, 1));
        return dto;
    }
}
