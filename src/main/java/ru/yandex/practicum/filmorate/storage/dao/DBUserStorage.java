package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnDeleteUserEvent;
import ru.yandex.practicum.filmorate.event.OnFeedEvent;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.AllowedFeedEvents;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
@Qualifier(DBStorageConsts.QUALIFIER)
public class DBUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final ApplicationEventPublisher eventPublisher;

    public DBUserStorage(
            JdbcTemplate jdbcTemplate,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean containsUser(int userId) {
        SqlRowSet result = jdbcTemplate.queryForRowSet("select USERID from USERS where USERID = ?;", userId);
        return result.next();
    }

    @Override
    public User getUser(Integer id) {
        String sqlUser = "select * from USERS where USERID = ?";
        User user;
        try {
            user = jdbcTemplate.queryForObject(sqlUser, (rs, rowNum) -> makeUser(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с идентификатором " +
                    id + " не зарегистрирован!");
        }
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        String sqlAllUsers = "select * from USERS";
        return jdbcTemplate.query(sqlAllUsers, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public User addUser(User user) {
        String sqlQuery = "insert into USERS " +
                "(EMAIL, LOGIN, NAME, BIRTHDAY) " +
                "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));

            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        if (user.getFriends() != null) {
            for (Integer friendId : user.getFriends()) {
                addFriend(user.getId(), friendId);
            }
        }
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlUser = "update USERS set " +
                "EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " +
                "where USERID = ?";
        jdbcTemplate.update(sqlUser,
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        return getUser(user.getId());
    }

    @Override
    public boolean deleteUser(int userId) {
        String sqlQuery = "delete from USERS where USERID = ?";
        boolean result = jdbcTemplate.update(sqlQuery, userId) > 0;
        if (result) {
            eventPublisher.publishEvent(new OnDeleteUserEvent(userId));
        }
        return result;
    }

    @Override
    public boolean addFriend(int userId, int friendId) {
        boolean friendAccepted;
        String sqlGetReversFriend = "select * from FRIENDSHIP " +
                "where USERID = ? and FRIENDID = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlGetReversFriend, friendId, userId);
        friendAccepted = rs.next();
        String sqlSetFriend = "insert into FRIENDSHIP (USERID, FRIENDID, STATUS) " +
                "VALUES (?,?,?)";
        boolean result = jdbcTemplate.update(sqlSetFriend, userId, friendId, friendAccepted) > 0;
        if (friendAccepted) {
            String sqlSetStatus = "update FRIENDSHIP set STATUS = true " +
                    "where USERID = ? and FRIENDID = ?";
            jdbcTemplate.update(sqlSetStatus, friendId, userId);
        }
        if (result) {
            eventPublisher.publishEvent(new OnFeedEvent(userId, friendId, AllowedFeedEvents.ADD_FRIEND));
        }
        return true;
    }

    @Override
    public boolean deleteFriend(int userId, int friendId) {
        String sqlDeleteFriend = "delete from FRIENDSHIP where USERID = ? and FRIENDID = ?";
        boolean result = jdbcTemplate.update(sqlDeleteFriend, userId, friendId) > 0;
        String sqlSetStatus = "update FRIENDSHIP set STATUS = false " +
                "where USERID = ? and FRIENDID = ?";
        jdbcTemplate.update(sqlSetStatus, friendId, userId);
        if (result) {
            eventPublisher.publishEvent(new OnFeedEvent(userId, friendId, AllowedFeedEvents.REMOVE_FRIEND));
        }
        return true;
    }

    @EventListener
    public void handleOnFeedEvent(OnFeedEvent event) {
        LocalDateTime timestamp = LocalDateTime.now();
        int userId = event.getUserId();
        int type = event.getFeedDetails().getType().getId();
        int operation = event.getFeedDetails().getOperation().getId();
        int entityId = event.getEntityId();

        String sqlFeedEvent =
                "insert into EVENTS (EVENTTIMESTAMP, USERID, EVENTTYPE, OPERATION, ENTITYID) " +
                        "values ( ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlFeedEvent, timestamp, userId, type, operation, entityId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("UserID");
        User user = new User(
                userId,
                rs.getString("Email"),
                rs.getString("Login"),
                rs.getString("Name"),
                Objects.requireNonNull(rs.getDate("BirthDay")).toLocalDate(),
                getUserFriends(userId));
        return user;
    }

    private List<Integer> getUserFriends(int userId) {
        String sqlGetFriends = "select FRIENDID from FRIENDSHIP where USERID = ?";
        return jdbcTemplate.queryForList(sqlGetFriends, Integer.class, userId);
    }

}
