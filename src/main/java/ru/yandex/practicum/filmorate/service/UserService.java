package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        prepareUserName(user);
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        prepareUserName(user);

        if (user.getId() == null) {
            throw new ValidationException("User id must not be null");
        }

        if (!userStorage.existsById(user.getId())) {
            throw new NotFoundException("User with id=" + user.getId() + " not found");
        }

        return userStorage.update(user);
    }

    public User getUserById(Integer id) {
        User user = userStorage.getById(id);

        if (user == null) {
            throw new NotFoundException("User with id=" + id + " not found");
        }

        return user;
    }

    public void addFriend(Integer id, Integer friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    public List<User> getFriends(Integer id) {
        User user = getUserById(id);

        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        User user = getUserById(id);
        User otherUser = getUserById(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    private void prepareUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}