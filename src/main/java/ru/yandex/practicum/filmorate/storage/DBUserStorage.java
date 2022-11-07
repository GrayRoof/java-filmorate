package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Component
public class DBUserStorage implements UserStorage{
    private final JdbcTemplate jdbcTemplate;

    public DBUserStorage(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public User getUser(Integer id) {
        return null;
    }

    @Override
    public Collection<User> getAllUsers() {
        return null;
    }

    @Override
    public User addUser(User user) {
        return null;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public boolean deleteUser(User user) {
        return false;
    }
}
