package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {

        if (!filmStorage.existsById(film.getId())) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found");
        }

        return filmStorage.update(film);
    }

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getById(id);

        if (film == null) {
            throw new NotFoundException("Film with id=" + id + " not found");
        }

        return film;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        film.getLikes().add(userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}