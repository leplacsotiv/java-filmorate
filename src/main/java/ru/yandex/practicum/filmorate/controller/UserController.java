package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        prepareUserName(user);

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Created user: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        prepareUserName(user);

        if (user.getId() == null) {
            log.warn("Validation failed: user id is null");
            throw new ValidationException("User id must not be null");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Update failed: user with id={} not found", user.getId());
            throw new ValidationException("User with id=" + user.getId() + " not found");
        }

        users.put(user.getId(), user);

        log.info("Updated user: {}", user);
        return user;
    }

    private void prepareUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}