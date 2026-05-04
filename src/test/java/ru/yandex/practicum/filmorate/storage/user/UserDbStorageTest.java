package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    UserDbStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void shouldCreateUser() {
        User user = createUser("user1@example.com", "user1", "User One");

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();

        User found = userStorage.getById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("user1@example.com");
        assertThat(found.getLogin()).isEqualTo("user1");
        assertThat(found.getName()).isEqualTo("User One");
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.create(createUser("old@example.com", "old", "Old Name"));

        user.setEmail("new@example.com");
        user.setLogin("new");
        user.setName("New Name");
        user.setBirthday(LocalDate.of(1999, 1, 1));

        User updated = userStorage.update(user);

        assertThat(updated.getId()).isEqualTo(user.getId());

        User found = userStorage.getById(user.getId());

        assertThat(found.getEmail()).isEqualTo("new@example.com");
        assertThat(found.getLogin()).isEqualTo("new");
        assertThat(found.getName()).isEqualTo("New Name");
        assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1999, 1, 1));
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(createUser("user1@example.com", "user1", "User One"));
        userStorage.create(createUser("user2@example.com", "user2", "User Two"));

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldReturnNullWhenUserNotFound() {
        User user = userStorage.getById(999);

        assertThat(user).isNull();
    }

    @Test
    void shouldCheckUserExistsById() {
        User user = userStorage.create(createUser("user1@example.com", "user1", "User One"));

        assertThat(userStorage.existsById(user.getId())).isTrue();
        assertThat(userStorage.existsById(999)).isFalse();
    }

    @Test
    void shouldAddAndGetFriends() {
        User user = userStorage.create(createUser("user1@example.com", "user1", "User One"));
        User friend = userStorage.create(createUser("user2@example.com", "user2", "User Two"));

        userStorage.addFriend(user.getId(), friend.getId());

        List<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(friend.getId());

        List<User> reverseFriends = userStorage.getFriends(friend.getId());

        assertThat(reverseFriends).isEmpty();
    }

    @Test
    void shouldRemoveFriend() {
        User user = userStorage.create(createUser("user1@example.com", "user1", "User One"));
        User friend = userStorage.create(createUser("user2@example.com", "user2", "User Two"));

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.removeFriend(user.getId(), friend.getId());

        List<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User user1 = userStorage.create(createUser("user1@example.com", "user1", "User One"));
        User user2 = userStorage.create(createUser("user2@example.com", "user2", "User Two"));
        User commonFriend = userStorage.create(createUser("user3@example.com", "user3", "User Three"));

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(commonFriend.getId());
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}