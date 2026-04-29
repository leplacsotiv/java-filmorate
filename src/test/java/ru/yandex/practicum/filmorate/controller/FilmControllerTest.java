package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmService filmService;

    @Test
    @DisplayName("PUT /films/{id}/like/{userId} should return 200 when film and user exist")
    void shouldAddLikeWhenFilmAndUserExist() throws Exception {
        doNothing().when(filmService).addLike(1, 2);

        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /films/{id}/like/{userId} should return 404 when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExistForLike() throws Exception {
        doThrow(new NotFoundException("User with id=999 not found"))
                .when(filmService).addLike(1, 999);

        mockMvc.perform(put("/films/1/like/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with id=999 not found"));
    }

    @Test
    @DisplayName("DELETE /films/{id}/like/{userId} should return 200 when film and user exist")
    void shouldRemoveLikeWhenFilmAndUserExist() throws Exception {
        doNothing().when(filmService).removeLike(1, 2);

        mockMvc.perform(delete("/films/1/like/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /films/popular?count={count} should return 200 and popular films list")
    void shouldReturnPopularFilmsWithCustomCount() throws Exception {
        Film film = new Film();
        film.setId(1);
        film.setName("Inception");
        film.setDescription("Dreams inside dreams");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        when(filmService.getPopularFilms(5)).thenReturn(List.of(film));

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Inception"))
                .andExpect(jsonPath("$[0].description").value("Dreams inside dreams"))
                .andExpect(jsonPath("$[0].releaseDate").value("2010-07-16"))
                .andExpect(jsonPath("$[0].duration").value(148));
    }

    @Test
    @DisplayName("GET /films/popular should use default count=10")
    void shouldUseDefaultCountForPopularFilms() throws Exception {
        when(filmService.getPopularFilms(10)).thenReturn(List.of());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /films/{id} should return 200 and film when film exists")
    void shouldReturnFilmByIdWhenFilmExists() throws Exception {
        Film film = new Film();
        film.setId(1);
        film.setName("Inception");
        film.setDescription("Dreams inside dreams");
        film.setReleaseDate(java.time.LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        when(filmService.getFilmById(1)).thenReturn(film);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inception"))
                .andExpect(jsonPath("$.description").value("Dreams inside dreams"))
                .andExpect(jsonPath("$.releaseDate").value("2010-07-16"))
                .andExpect(jsonPath("$.duration").value(148));
    }

    @Test
    @DisplayName("GET /films/{id} should return 404 when film does not exist")
    void shouldReturnNotFoundWhenFilmDoesNotExist() throws Exception {
        when(filmService.getFilmById(999))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Film with id=999 not found"));

        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Film with id=999 not found"));
    }

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

        Film createdFilm = new Film();
        createdFilm.setId(1);
        createdFilm.setName("Inception");
        createdFilm.setDescription("Dreams inside dreams");
        createdFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        createdFilm.setDuration(148);

        when(filmService.createFilm(any(Film.class))).thenReturn(createdFilm);

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