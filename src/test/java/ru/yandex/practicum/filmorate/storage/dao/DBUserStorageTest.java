package ru.yandex.practicum.filmorate.storage.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorageTestHelper;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorageTestHelper;

import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class DBUserStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final UserStorageTestHelper userStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;

    @Autowired
    public DBUserStorageTest(
            JdbcTemplate jdbcTemplate,
            DBUserStorage userStorage,
            FilmStorage filmStorage
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;

        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM friendship;");
        jdbcTemplate.update("DELETE FROM likes;");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("DELETE FROM film;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN userid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN filmid RESTART WITH 1;");
    }

    @Test
    void deleteUser() {
        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        assertTrue(userStorage.containsUser(annId));
        assertTrue(userStorage.containsUser(bobId));

        userStorage.deleteUser(bobId);

        assertTrue(userStorage.containsUser(annId));
        assertFalse(userStorage.containsUser(bobId));
    }

    @Test
    void deleteUserDeletesActiveFriendship() {
        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();
        userStorage.addFriend(camId, annId);
        userStorage.addFriend(camId, bobId);

        Supplier<Integer> camFriendsCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM friendship WHERE userid=?;",
                        Integer.class,
                        camId
                );
        assertEquals(2, camFriendsCount.get());

        userStorage.deleteUser(camId);

        assertEquals(0, camFriendsCount.get());
    }

    @Test
    void deleteUserDeletesPassiveFriendship() {
        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();
        userStorage.addFriend(annId, camId);
        userStorage.addFriend(bobId, camId);

        Supplier<Integer> camFriendsCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM friendship WHERE friendid=?;",
                        Integer.class,
                        camId
                );
        assertEquals(2, camFriendsCount.get());

        userStorage.deleteUser(camId);

        assertEquals(0, camFriendsCount.get());
    }

    @Test
    void deleteUserDeletesLikes() {
        final int amelieId = filmStorageTestHelper.getNewFilmId();
        final int batmanId = filmStorageTestHelper.getNewFilmId();
        final int carrieId = filmStorageTestHelper.getNewFilmId();

        final int userId = userStorageTestHelper.getNewUserId();
        filmStorage.addLike(amelieId, userId);
        filmStorage.addLike(batmanId, userId);
        filmStorage.addLike(carrieId, userId);

        Supplier<Integer> userLikesCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM likes WHERE USERID=?;",
                        Integer.class,
                        userId
                );
        assertEquals(3, userLikesCount.get());

        userStorage.deleteUser(userId);

        assertEquals(0, userLikesCount.get());
    }

    @Test
    void deleteUserUpdatesFilmRate() {
        final int filmId = filmStorageTestHelper.getNewFilmId();

        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();
        filmStorage.addLike(filmId, annId);
        filmStorage.addLike(filmId, bobId);
        filmStorage.addLike(filmId, camId);

        Supplier<Integer> filmRate =
                () -> jdbcTemplate.queryForObject(
                        "SELECT rate FROM film WHERE filmid=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(3, filmRate.get());
        userStorage.deleteUser(camId);
        assertEquals(2, filmRate.get());
    }

    @Test
    void shouldReturnFeed() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int firstUserId = userStorageTestHelper.getNewUserId();
        final int secondUserId = userStorageTestHelper.getNewUserId();
        final int thirdUserId = userStorageTestHelper.getNewUserId();
        filmStorage.addLike(filmId, firstUserId);
        userStorage.addFriend(thirdUserId, firstUserId);
        userStorage.addFriend(firstUserId, secondUserId);


        FeedEvent[] actual = userStorage.getFeed(firstUserId).toArray(FeedEvent[]::new);
        assertEquals(2, actual.length, "Количество объектов в ленте не совпадает с ожидаемым");
        assertEquals(filmId, actual[0].getEntityId(), "id сущности для первого события не тот");
        assertEquals(1, actual[0].getEventId(), "id первого события не тот");
        assertEquals("LIKE", actual[0].getEventType().toString(), "Неверный тип первого события");
        assertEquals("ADD", actual[0].getOperation().toString(), "Неверная операция первого события");

        assertEquals(secondUserId, actual[1].getEntityId(), "id сущности для второго события не тот");
        assertEquals(3, actual[1].getEventId(), "id второго события не тот");
        assertEquals("FRIEND", actual[1].getEventType().toString(), "Неверный тип второго события");
        assertEquals("ADD", actual[1].getOperation().toString(), "Неверная операция второго события");
    }

}