package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    boolean contains(int userId);
    User get(final Integer id);
    Collection<User> getAll();
    User add(User user);
    User update(User user);
    boolean delete(int userId);
    boolean addFriend(int userId, int friendId);
    boolean deleteFriend(int userId, int friendId);
}