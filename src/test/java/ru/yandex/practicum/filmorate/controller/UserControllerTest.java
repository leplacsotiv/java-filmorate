package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("PUT /users/{id}/friends/{friendId} should return 200 when users exist")
    void shouldAddFriendWhenUsersExist() throws Exception {
        doNothing().when(userService).addFriend(1, 2);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /users/{id}/friends/{friendId} should return 404 when friend does not exist")
    void shouldReturnNotFoundWhenFriendDoesNotExist() throws Exception {
        doThrow(new NotFoundException("User with id=999 not found"))
                .when(userService).addFriend(1, 999);

        mockMvc.perform(put("/users/1/friends/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with id=999 not found"));
    }

    @Test
    @DisplayName("DELETE /users/{id}/friends/{friendId} should return 200 when users exist")
    void shouldRemoveFriendWhenUsersExist() throws Exception {
        doNothing().when(userService).removeFriend(1, 2);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/{id}/friends should return 200 and friends list")
    void shouldReturnFriendsList() throws Exception {
        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(1990, 1, 1));

        when(userService.getFriends(1)).thenReturn(List.of(friend));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].email").value("friend@test.com"))
                .andExpect(jsonPath("$[0].login").value("friend"))
                .andExpect(jsonPath("$[0].name").value("Friend"))
                .andExpect(jsonPath("$[0].birthday").value("1990-01-01"));
    }

    @Test
    @DisplayName("GET /users/{id}/friends/common/{otherId} should return 200 and common friends list")
    void shouldReturnCommonFriends() throws Exception {
        User commonFriend = new User();
        commonFriend.setId(3);
        commonFriend.setEmail("common@test.com");
        commonFriend.setLogin("common");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1992, 2, 2));

        when(userService.getCommonFriends(1, 2)).thenReturn(List.of(commonFriend));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].email").value("common@test.com"))
                .andExpect(jsonPath("$[0].login").value("common"))
                .andExpect(jsonPath("$[0].name").value("Common Friend"))
                .andExpect(jsonPath("$[0].birthday").value("1992-02-02"));
    }

    @Test
    @DisplayName("GET /users/{id} should return 200 and user when user exists")
    void shouldReturnUserByIdWhenUserExists() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setName("Ivan");
        user.setBirthday(java.time.LocalDate.of(1995, 10, 10));

        when(userService.getUserById(1)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.login").value("ivan"))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.birthday").value("1995-10-10"));
    }

    @Test
    @DisplayName("GET /users/{id} should return 404 when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getUserById(999))
                .thenThrow(new NotFoundException("User with id=999 not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with id=999 not found"));
    }

    @Test
    @DisplayName("POST /users should return 400 when email is invalid")
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        String json = "{\n" +
                "  \"email\": \"testmail.com\",\n" +
                "  \"login\": \"ivan\",\n" +
                "  \"name\": \"Ivan\",\n" +
                "  \"birthday\": \"1995-10-10\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email must be valid"));
    }

    @Test
    @DisplayName("POST /users should return 400 when login is blank")
    void shouldReturnBadRequestWhenLoginIsBlank() throws Exception {
        String json = "{\n" +
                "  \"email\": \"test@test.com\",\n" +
                "  \"login\": \"\",\n" +
                "  \"name\": \"Ivan\",\n" +
                "  \"birthday\": \"1995-10-10\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Login must not be blank"));
    }

    @Test
    @DisplayName("POST /users should return 400 when login contains spaces")
    void shouldReturnBadRequestWhenLoginContainsSpaces() throws Exception {
        String json = "{\n" +
                "  \"email\": \"test@test.com\",\n" +
                "  \"login\": \"ivan petrov\",\n" +
                "  \"name\": \"Ivan\",\n" +
                "  \"birthday\": \"1995-10-10\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Login must not contain spaces"));
    }

    @Test
    @DisplayName("POST /users should return 400 when birthday is in the future")
    void shouldReturnBadRequestWhenBirthdayIsInFuture() throws Exception {
        String json = "{\n" +
                "  \"email\": \"test@test.com\",\n" +
                "  \"login\": \"ivan\",\n" +
                "  \"name\": \"Ivan\",\n" +
                "  \"birthday\": \"2999-01-01\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Birthday must not be in the future"));
    }

    @Test
    @DisplayName("POST /users should replace blank name with login")
    void shouldReplaceBlankNameWithLogin() throws Exception {
        String json = "{\n" +
                "  \"email\": \"test@test.com\",\n" +
                "  \"login\": \"ivan\",\n" +
                "  \"name\": \"\",\n" +
                "  \"birthday\": \"1995-10-10\"\n" +
                "}";

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setEmail("test@test.com");
        createdUser.setLogin("ivan");
        createdUser.setName("ivan");
        createdUser.setBirthday(LocalDate.of(1995, 10, 10));

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.login").value("ivan"))
                .andExpect(jsonPath("$.name").value("ivan"))
                .andExpect(jsonPath("$.birthday").value("1995-10-10"));
    }
}