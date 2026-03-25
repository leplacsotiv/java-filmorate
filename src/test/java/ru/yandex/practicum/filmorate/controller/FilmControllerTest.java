package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /films should return 400 when film name is blank")
    void shouldReturnBadRequestWhenFilmNameIsBlank() throws Exception {
        String json = "{\n" +
                "  \"name\": \"\",\n" +
                "  \"description\": \"Good description\",\n" +
                "  \"releaseDate\": \"2000-01-01\",\n" +
                "  \"duration\": 120\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Film name must not be blank"));
    }

    @Test
    @DisplayName("POST /films should return 400 when description is longer than 200 characters")
    void shouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        String longDescription = "a".repeat(201);

        String json = "{\n" +
                "  \"name\": \"Film\",\n" +
                "  \"description\": \"" + longDescription + "\",\n" +
                "  \"releaseDate\": \"2000-01-01\",\n" +
                "  \"duration\": 120\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Description must be <= 200 characters"));
    }

    @Test
    @DisplayName("POST /films should return 400 when release date is before 1895-12-28")
    void shouldReturnBadRequestWhenReleaseDateIsTooEarly() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Film\",\n" +
                "  \"description\": \"Good description\",\n" +
                "  \"releaseDate\": \"1895-12-27\",\n" +
                "  \"duration\": 120\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Release date must not be earlier than 1895-12-28"));
    }

    @Test
    @DisplayName("POST /films should return 400 when duration is not positive")
    void shouldReturnBadRequestWhenDurationIsNotPositive() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Film\",\n" +
                "  \"description\": \"Good description\",\n" +
                "  \"releaseDate\": \"2000-01-01\",\n" +
                "  \"duration\": 0\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Duration must be positive"));
    }

    @Test
    @DisplayName("POST /films should return 200 and created film when request is valid")
    void shouldCreateFilmWhenRequestIsValid() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Inception\",\n" +
                "  \"description\": \"Dreams inside dreams\",\n" +
                "  \"releaseDate\": \"2010-07-16\",\n" +
                "  \"duration\": 148\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inception"))
                .andExpect(jsonPath("$.description").value("Dreams inside dreams"))
                .andExpect(jsonPath("$.releaseDate").value("2010-07-16"))
                .andExpect(jsonPath("$.duration").value(148));
    }
}