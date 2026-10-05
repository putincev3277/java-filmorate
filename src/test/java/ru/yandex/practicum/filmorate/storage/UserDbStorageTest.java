package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate.storage")
@Slf4j
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User createUser(String login, String email) {
        return User.builder()
                .email(email)
                .login(login)
                .name(login)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldCreateAndFindUserById() {
        User created = userStorage.create(createUser("testUser", "test@example.com"));

        assertThat(created.getId()).isNotNull();

        Optional<User> fromDb = userStorage.findById(created.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo("test@example.com");
                    assertThat(u.getLogin()).isEqualTo("testUser");
                    assertThat(u.getName()).isEqualTo("testUser");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertThat(userStorage.findById(9999L)).isEmpty();
    }

    @Test
    void shouldReplaceEmptyNameWithLogin() {
        User created = userStorage.create(User.builder()
                .email("blank@example.com")
                .login("blankName")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        // Тут логика зависит от твоего кода: если ты меняешь name на login внутри create,
        // то здесь должно быть "blankName" (если не меняешь) или "blankName" (если меняешь).
        // Проверь, что именно делает твой метод create.
        assertThat(created.getName()).isEqualTo("blankName");
    }

    @Test
    void shouldUpdateUser() {
        User created = userStorage.create(createUser("before", "before@example.com"));

        created.setLogin("after");
        created.setEmail("after@example.com");
        created.setName("After");
        created.setBirthday(LocalDate.of(1995, 5, 5));

        userStorage.update(created.getId(), created);

        User fromDb = userStorage.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getLogin()).isEqualTo("after");
        assertThat(fromDb.getEmail()).isEqualTo("after@example.com");
        assertThat(fromDb.getName()).isEqualTo("After");
        assertThat(fromDb.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    void shouldGetAllUsers() {
        userStorage.create(createUser("user1", "u1@example.com"));
        userStorage.create(createUser("user2", "u2@example.com"));

        assertThat(userStorage.getAll())
                .extracting(User::getLogin)
                .contains("user1", "user2");
    }

    @Test
    void shouldCheckEmailAndLoginExistence() {
        userStorage.create(createUser("uniqueLogin", "unique@example.com"));

        assertThat(userStorage.existsByEmail("unique@example.com")).isTrue();
        assertThat(userStorage.existsByEmail("nope@example.com")).isFalse();
        assertThat(userStorage.existsByLogin("uniqueLogin")).isTrue();
        assertThat(userStorage.existsByLogin("nopeLogin")).isFalse();
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User u1 = userStorage.create(createUser("friend1", "f1@example.com"));
        User u2 = userStorage.create(createUser("friend2", "f2@example.com"));

        userStorage.addFriend(u1.getId(), u2.getId());
        assertThat(userStorage.getFriendsIds(u1.getId())).containsExactly(u2.getId());

        userStorage.removeFriend(u1.getId(), u2.getId());
        assertThat(userStorage.getFriendsIds(u1.getId())).isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User u1 = userStorage.create(createUser("common1", "c1@example.com"));
        User u2 = userStorage.create(createUser("common2", "c2@example.com"));
        User u3 = userStorage.create(createUser("common3", "c3@example.com"));

        userStorage.addFriend(u1.getId(), u3.getId());
        userStorage.addFriend(u2.getId(), u3.getId());

        List<User> common = userStorage.getCommonFriends(u1.getId(), u2.getId());
        assertThat(common).extracting(User::getId).containsExactly(u3.getId());
    }
}
