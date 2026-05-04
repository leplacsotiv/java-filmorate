package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.LinkedHashSet;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public Collection<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        prepareFilm(film);
        validateFilmReferences(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.existsById(film.getId())) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found");
        }

        prepareFilm(film);
        validateFilmReferences(film);
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
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    private void validateFilmReferences(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            if (!mpaStorage.existsById(film.getMpa().getId())) {
                throw new NotFoundException("MPA rating with id=" + film.getMpa().getId() + " not found");
            }
        }

        for (Genre genre : film.getGenres()) {
            if (genre == null || genre.getId() == null) {
                throw new ValidationException("Genre id must not be null");
            }

            if (!genreStorage.existsById(genre.getId())) {
                throw new NotFoundException("Genre with id=" + genre.getId() + " not found");
            }
        }
    }

    private void prepareFilm(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }
    }
}