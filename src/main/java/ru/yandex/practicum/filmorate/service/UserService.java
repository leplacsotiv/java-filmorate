package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        prepareUserName(user);
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        prepareUserName(user);

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
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }

        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("User with id=" + friendId + " not found");
        }

        userStorage.addFriend(id, friendId);
    }

    public void removeFriend(Integer id, Integer friendId) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }

        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("User with id=" + friendId + " not found");
        }

        userStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(Integer id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }

        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }

        if (!userStorage.existsById(otherId)) {
            throw new NotFoundException("User with id=" + otherId + " not found");
        }

        return userStorage.getCommonFriends(id, otherId);
    }

    private void prepareUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}