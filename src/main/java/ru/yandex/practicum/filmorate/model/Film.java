package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.MinReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private Integer id;

    @NotBlank(message = "Film name must not be blank")
    private String name;

    @Size(max = 200, message = "Description must be <= 200 characters")
    private String description;

    @NotNull(message = "Release date must not be null")
    @MinReleaseDate
    private LocalDate releaseDate;

    @NotNull(message = "Duration must not be null")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    private Set<Integer> likes = new HashSet<>();
}