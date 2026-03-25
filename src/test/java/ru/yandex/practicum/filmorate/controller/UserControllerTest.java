package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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