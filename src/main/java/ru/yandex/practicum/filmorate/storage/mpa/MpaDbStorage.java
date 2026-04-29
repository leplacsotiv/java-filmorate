package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_rating_id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };

    @Override
    public List<Mpa> findAll() {
        String sql = """
                SELECT mpa_rating_id, name
                FROM mpa_ratings
                ORDER BY mpa_rating_id
                """;

        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Mpa getById(Integer id) {
        String sql = """
                SELECT mpa_rating_id, name
                FROM mpa_ratings
                WHERE mpa_rating_id = ?
                """;

        List<Mpa> ratings = jdbcTemplate.query(sql, mpaRowMapper, id);

        if (ratings.isEmpty()) {
            return null;
        }

        return ratings.getFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = """
                SELECT COUNT(*)
                FROM mpa_ratings
                WHERE mpa_rating_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}