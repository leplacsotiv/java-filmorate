package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_rating_id");
        if (!rs.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }

        return film;
    };

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT
                    f.film_id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.mpa_rating_id,
                    m.name AS mpa_name
                FROM films AS f
                LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.mpa_rating_id
                ORDER BY f.film_id
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        loadGenresForFilms(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setObject(5, null);
            }

            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_rating_id = ?
                WHERE film_id = ?
                """;

        Integer mpaId = null;
        if (film.getMpa() != null) {
            mpaId = film.getMpa().getId();
        }

        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId()
        );

        deleteGenres(film.getId());
        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(Integer id) {
        String sql = """
                SELECT
                    f.film_id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.mpa_rating_id,
                    m.name AS mpa_name
                FROM films AS f
                LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.mpa_rating_id
                WHERE f.film_id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);

        if (films.isEmpty()) {
            return null;
        }

        Film film = films.get(0);
        loadGenres(film);
        return film;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = """
                SELECT COUNT(*)
                FROM films
                WHERE film_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String sql = """
            MERGE INTO likes (film_id, user_id)
            KEY (film_id, user_id)
            VALUES (?, ?)
            """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        String sql = """
            DELETE FROM likes
            WHERE film_id = ?
              AND user_id = ?
            """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
            SELECT
                f.film_id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_rating_id,
                m.name AS mpa_name,
                COUNT(l.user_id) AS likes_count
            FROM films AS f
            LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.mpa_rating_id
            LEFT JOIN likes AS l ON f.film_id = l.film_id
            GROUP BY
                f.film_id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_rating_id,
                m.name
            ORDER BY likes_count DESC, f.film_id
            LIMIT ?
            """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        loadGenresForFilms(films);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        Set<Integer> genreIds = new LinkedHashSet<>();

        for (Genre genre : film.getGenres()) {
            if (genre != null && genre.getId() != null) {
                genreIds.add(genre.getId());
            }
        }

        for (Integer genreId : genreIds) {
            jdbcTemplate.update(sql, film.getId(), genreId);
        }
    }

    private void deleteGenres(Integer filmId) {
        String sql = """
                DELETE FROM film_genres
                WHERE film_id = ?
                """;

        jdbcTemplate.update(sql, filmId);
    }

    private void loadGenres(Film film) {
        String sql = """
                SELECT
                    g.genre_id,
                    g.name
                FROM genres AS g
                JOIN film_genres AS fg ON g.genre_id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.genre_id
                """;

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        films.forEach(film -> film.setGenres(new LinkedHashSet<>()));

        String placeholders = films.stream()
                .map(film -> "?")
                .collect(Collectors.joining(", "));

        String sql = """
            SELECT 
                fg.film_id,
                g.genre_id,
                g.name
            FROM film_genres AS fg
            JOIN genres AS g ON fg.genre_id = g.genre_id
            WHERE fg.film_id IN (%s)
            ORDER BY fg.film_id, g.genre_id
            """.formatted(placeholders);

        Map<Integer, Film> filmsById = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        Object[] filmIds = films.stream()
                .map(Film::getId)
                .toArray();

        jdbcTemplate.query(sql, rs -> {
            Integer filmId = rs.getInt("film_id");

            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));

            filmsById.get(filmId).getGenres().add(genre);
        }, filmIds);
    }
}