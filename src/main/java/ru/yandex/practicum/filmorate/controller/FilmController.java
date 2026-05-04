package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;
import jakarta.validation.constraints.Positive;

import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        Film film = filmService.getFilmById(id);
        log.info("Received film by id={}", id);
        return film;
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        Film createdFilm = filmService.createFilm(film);
        log.info("Created film: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Validated(UpdateValidationGroup.class) @RequestBody Film film) {
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Updated film: {}", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
        log.info("User with id={} added like to film with id={}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
        log.info("User with id={} removed like from film with id={}", userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@Positive(message = "Count must be positive")
                                      @RequestParam(defaultValue = "10") int count) {
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.info("Received {} popular films", count);
        return popularFilms;
    }
}