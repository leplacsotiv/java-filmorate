package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));

        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }

        return user;
    };

    @Override
    public Collection<User> findAll() {
        String sql = """
                SELECT user_id, email, login, name, birthday
                FROM users
                ORDER BY user_id
                """;

        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());

            if (user.getBirthday() != null) {
                ps.setDate(4, Date.valueOf(user.getBirthday()));
            } else {
                ps.setDate(4, null);
            }

            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET email = ?,
                    login = ?,
                    name = ?,
                    birthday = ?
                WHERE user_id = ?
                """;

        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        return user;
    }

    @Override
    public User getById(Integer id) {
        String sql = """
                SELECT user_id, email, login, name, birthday
                FROM users
                WHERE user_id = ?
                """;

        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE user_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String sql = """
            MERGE INTO friendships (user_id, friend_id, status_id)
            KEY (user_id, friend_id)
            VALUES (?, ?, 1)
            """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        String sql = """
            DELETE FROM friendships
            WHERE user_id = ?
              AND friend_id = ?
            """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        String sql = """
            SELECT 
                u.user_id,
                u.email,
                u.login,
                u.name,
                u.birthday
            FROM users AS u
            JOIN friendships AS f ON u.user_id = f.friend_id
            WHERE f.user_id = ?
            ORDER BY u.user_id
            """;

        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        String sql = """
            SELECT 
                u.user_id,
                u.email,
                u.login,
                u.name,
                u.birthday
            FROM users AS u
            JOIN friendships AS f1 ON u.user_id = f1.friend_id
            JOIN friendships AS f2 ON u.user_id = f2.friend_id
            WHERE f1.user_id = ?
              AND f2.user_id = ?
            ORDER BY u.user_id
            """;

        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }
}