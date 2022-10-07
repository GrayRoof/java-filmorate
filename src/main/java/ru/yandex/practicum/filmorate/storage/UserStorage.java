package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getAllUsers();
    User addUser(User user);
    User updateUser(User user);
    boolean deleteUser(User user);

}
