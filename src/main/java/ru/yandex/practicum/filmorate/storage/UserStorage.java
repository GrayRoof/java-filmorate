package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    boolean containsUser(int userId);

    User getUser(final Integer id);

    Collection<User> getAllUsers();

    User addUser(User user);

    User updateUser(User user);

    boolean deleteUser(int userId);

    boolean addFriend(int userId, int friendId);

    boolean deleteFriend(int userId, int friendId);


}
