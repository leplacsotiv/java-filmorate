package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Autowired
    MpaDbStorageTest(MpaDbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @Test
    void shouldFindAllMpaRatings() {
        List<Mpa> ratings = mpaStorage.findAll();

        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0))
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    void shouldFindMpaById() {
        Mpa mpa = mpaStorage.getById(1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void shouldReturnNullWhenMpaNotFound() {
        Mpa mpa = mpaStorage.getById(999);

        assertThat(mpa).isNull();
    }

    @Test
    void shouldCheckMpaExistsById() {
        assertThat(mpaStorage.existsById(1)).isTrue();
        assertThat(mpaStorage.existsById(999)).isFalse();
    }
}