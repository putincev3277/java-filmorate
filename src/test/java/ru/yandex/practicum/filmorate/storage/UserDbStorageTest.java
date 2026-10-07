package ru.yandex.practicum.filmorate.storage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        // Инициализируем хранилище с нужным JdbcTemplate
        userStorage = new UserDbStorage(jdbcTemplate);

        // Чистим таблицы перед каждым тестом (порядок важен из-за внешних ключей)
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");
    }

    private User createUser(String prefix) {
        String suffix = String.valueOf(System.nanoTime());
        return userStorage.create(User.builder()
                .email(prefix + "_" + suffix + "@example.com")
                .login(prefix + "_" + suffix)
                .name(prefix + " User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
    }

    // --- Базовые операции ---

    @Test
    void shouldCreateUserWithGeneratedId() {
        User user = createUser("create");

        Assertions.assertThat(user.getId()).isNotNull().isPositive();

        User fromDb = userStorage.findById(user.getId()).orElseThrow();
        Assertions.assertThat(fromDb.getEmail()).isEqualTo(user.getEmail());
        Assertions.assertThat(fromDb.getLogin()).isEqualTo(user.getLogin());
        Assertions.assertThat(fromDb.getName()).isEqualTo(user.getName());
        Assertions.assertThat(fromDb.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldReturnEmptyForUnknownUser() {
        Assertions.assertThat(userStorage.findById(9999L)).isEmpty();
    }

    @Test
    void shouldReturnAllUsers() {
        createUser("first");
        createUser("second");

        List<User> all = userStorage.getAll();
        Assertions.assertThat(all).hasSize(2);
    }

    @Test
    void shouldUpdateUser() {
        User user = createUser("before");
        user.setEmail("updated_" + System.nanoTime() + "@example.com");
        user.setLogin("updated_" + System.nanoTime());
        user.setName("Updated Name");

        userStorage.update(user.getId(), user);

        User fromDb = userStorage.findById(user.getId()).orElseThrow();
        Assertions.assertThat(fromDb.getName()).isEqualTo("Updated Name");
        Assertions.assertThat(fromDb.getEmail()).isEqualTo(user.getEmail());
        Assertions.assertThat(fromDb.getLogin()).isEqualTo(user.getLogin());
    }

    // --- existsBy* ---

    @Test
    void shouldCheckEmailAndLoginExistence() {
        User user = createUser("exists");

        Assertions.assertThat(userStorage.existsByEmail(user.getEmail())).isTrue();
        Assertions.assertThat(userStorage.existsByEmail("no_such_email@example.com")).isFalse();
        Assertions.assertThat(userStorage.existsByLogin(user.getLogin())).isTrue();
        Assertions.assertThat(userStorage.existsByLogin("no_such_login")).isFalse();
    }

    // --- Друзья ---

    @Test
    void shouldAddOneWayFriendship() {
        User user = createUser("user");
        User friend = createUser("friend");

        userStorage.addFriend(user.getId(), friend.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, user.getId(), friend.getId());
        Assertions.assertThat(count).isEqualTo(1);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                String.class, user.getId(), friend.getId());
        Assertions.assertThat(status).isEqualTo("PENDING");
    }

    @Test
    void shouldConfirmFriendshipWhenBothAddEachOther() {
        User user1 = createUser("first");
        User user2 = createUser("second");

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), user1.getId());

        String status1 = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                String.class, user1.getId(), user2.getId());
        String status2 = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                String.class, user2.getId(), user1.getId());

        Assertions.assertThat(status1).isEqualTo("CONFIRMED");
        Assertions.assertThat(status2).isEqualTo("CONFIRMED");
    }

    @Test
    void shouldGetFriendsIdsAndFriends() {
        User user = createUser("user");
        User friend1 = createUser("friend1");
        User friend2 = createUser("friend2");

        userStorage.addFriend(user.getId(), friend1.getId());
        userStorage.addFriend(user.getId(), friend2.getId());

        Set<Long> friendIds = userStorage.getFriendsIds(user.getId());
        Assertions.assertThat(friendIds).containsExactlyInAnyOrder(friend1.getId(), friend2.getId());

        List<User> friends = userStorage.getFriends(user.getId());
        Assertions.assertThat(friends)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(friend1.getId(), friend2.getId());

        Assertions.assertThat(userStorage.getFriendsIds(friend1.getId())).isEmpty();
    }

    @Test
    void shouldRemoveFriendOneWayOnly() {
        User user = createUser("user");
        User friend = createUser("friend");

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.removeFriend(user.getId(), friend.getId());

        Assertions.assertThat(userStorage.getFriendsIds(user.getId())).isEmpty();
        Assertions.assertThat(userStorage.getFriendsIds(friend.getId())).isEmpty();
    }

    @Test
    void shouldNotRemoveOthersFriendship() {
        User user = createUser("user");
        User friend = createUser("friend");
        User other = createUser("other");

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(other.getId(), friend.getId());

        userStorage.removeFriend(user.getId(), friend.getId());

        Assertions.assertThat(userStorage.getFriendsIds(other.getId()))
                .containsExactly(friend.getId());
    }

    @Test
    void shouldFindCommonFriends() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");
        User commonFriend = createUser("common");
        User notCommon = createUser("notCommon");

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());
        userStorage.addFriend(user1.getId(), notCommon.getId()); // только у user1

        List<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());

        Assertions.assertThat(common)
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }
}
