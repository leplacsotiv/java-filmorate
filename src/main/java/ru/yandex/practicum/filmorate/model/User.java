package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.NoSpaces;
import ru.yandex.practicum.filmorate.validation.UpdateValidationGroup;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    @NotNull(message = "User id must not be null", groups = UpdateValidationGroup.class)
    private Integer id;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Login must not be blank")
    @NoSpaces
    private String login;

    private String name;

    @PastOrPresent(message = "Birthday must not be in the future")
    private LocalDate birthday;

    private Set<Integer> friends = new HashSet<>();
}