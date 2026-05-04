package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    FilmDbStorageTest(FilmDbStorage filmStorage, UserDbStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Test
    void shouldCreateFilm() {
        Film film = createFilm("Matrix");

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Matrix");
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getId()).isEqualTo(4);
        assertThat(created.getMpa().getName()).isEqualTo("R");
        assertThat(created.getGenres()).hasSize(2);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.create(createFilm("Matrix"));

        film.setName("Matrix Updated");
        film.setDescription("Updated description");
        film.setDuration(140);

        Mpa mpa = new Mpa();
        mpa.setId(3);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(6);
        film.setGenres(new LinkedHashSet<>(Set.of(genre)));

        Film updated = filmStorage.update(film);

        assertThat(updated.getId()).isEqualTo(film.getId());
        assertThat(updated.getName()).isEqualTo("Matrix Updated");
        assertThat(updated.getMpa().getName()).isEqualTo("PG-13");
        assertThat(updated.getGenres()).hasSize(1);
        assertThat(updated.getGenres().iterator().next().getId()).isEqualTo(6);
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(createFilm("Film One"));
        filmStorage.create(createFilm("Film Two"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldFindFilmById() {
        Film created = filmStorage.create(createFilm("Matrix"));

        Film found = filmStorage.getById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Matrix");
    }

    @Test
    void shouldReturnNullWhenFilmNotFound() {
        Film film = filmStorage.getById(999);

        assertThat(film).isNull();
    }

    @Test
    void shouldCheckFilmExistsById() {
        Film film = filmStorage.create(createFilm("Matrix"));

        assertThat(filmStorage.existsById(film.getId())).isTrue();
        assertThat(filmStorage.existsById(999)).isFalse();
    }

    @Test
    void shouldAddLikeAndGetPopularFilms() {
        User user = userStorage.create(createUser("user1@example.com", "user1", "User One"));

        Film film1 = filmStorage.create(createFilm("Film One"));
        Film film2 = filmStorage.create(createFilm("Film Two"));

        filmStorage.addLike(film2.getId(), user.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(10);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film2.getId());
    }

    @Test
    void shouldRemoveLike() {
        User user = userStorage.create(createUser("user1@example.com", "user1", "User One"));
        Film film = filmStorage.create(createFilm("Matrix"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(10);

        assertThat(popularFilms).hasSize(1);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(4);
        film.setMpa(mpa);

        Genre genre1 = new Genre();
        genre1.setId(4);

        Genre genre2 = new Genre();
        genre2.setId(6);

        film.setGenres(new LinkedHashSet<>(Set.of(genre1, genre2)));

        return film;
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