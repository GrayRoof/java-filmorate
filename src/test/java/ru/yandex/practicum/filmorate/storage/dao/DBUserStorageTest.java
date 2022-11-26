package ru.yandex.practicum.filmorate.storage.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class DBUserStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewStorage reviewStorage;
    private final UserStorageTestHelper userStorageTestHelper;
    private final ReviewStorageTestHelper reviewStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;

    @Autowired
    public DBUserStorageTest(
            JdbcTemplate jdbcTemplate,
            DBUserStorage userStorage,
            FilmStorage filmStorage,
            ReviewStorage reviewStorage
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.reviewStorage = reviewStorage;

        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
        this.reviewStorageTestHelper = new ReviewStorageTestHelper(reviewStorage);
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
    @Tag(DBTestTags.DB_LOW_LEVEL)
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
    @Tag(DBTestTags.DB_LOW_LEVEL)
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
    @Tag(DBTestTags.DB_LOW_LEVEL)
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
    @Tag(DBTestTags.DB_LOW_LEVEL)
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
    @Tag(DBTestTags.DB_LOW_LEVEL)
    void deleteUserDeletesReviews() {
        final int amelieId = filmStorageTestHelper.getNewFilmId();
        final int batmanId = filmStorageTestHelper.getNewFilmId();
        final int carrieId = filmStorageTestHelper.getNewFilmId();
        final int userId = userStorageTestHelper.getNewUserId();

        reviewStorageTestHelper.addReview(amelieId, userId, true);
        reviewStorageTestHelper.addReview(batmanId, userId, true);
        reviewStorageTestHelper.addReview(carrieId, userId, true);

        Supplier<Integer> userReviewsCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reviews WHERE userid=?;",
                        Integer.class,
                        userId
                );
        assertEquals(3, userReviewsCount.get());

        userStorage.deleteUser(userId);

        assertEquals(0, userReviewsCount.get());
    }

    @Test
    void deleteUserDeletesReviewScores() {
        final int amelieId = filmStorageTestHelper.getNewFilmId();
        final int batmanId = filmStorageTestHelper.getNewFilmId();
        final int carrieId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();

        final int userId = userStorageTestHelper.getNewUserId();

        final int amelieReviewId = reviewStorageTestHelper.getNewReviewId(amelieId, authorId, true);
        final int batmanReviewId = reviewStorageTestHelper.getNewReviewId(batmanId, authorId, true);
        final int carrieReviewId = reviewStorageTestHelper.getNewReviewId(carrieId, authorId, true);

        reviewStorage.setScoreFromUser(amelieReviewId, userId, true);
        reviewStorage.setScoreFromUser(batmanReviewId, userId, true);
        reviewStorage.setScoreFromUser(carrieReviewId, userId, true);

        Consumer<Boolean> checkUserRelatedData =
                (noReviewScoreSet) -> {
                    assertEquals(noReviewScoreSet, reviewStorage.getScoreFromUser(amelieReviewId, userId).isEmpty());
                    assertEquals(noReviewScoreSet, reviewStorage.getScoreFromUser(batmanReviewId, userId).isEmpty());
                    assertEquals(noReviewScoreSet, reviewStorage.getScoreFromUser(carrieReviewId, userId).isEmpty());

                    int expectedReviewUseful = noReviewScoreSet ? 0 : 1;
                    assertEquals(expectedReviewUseful, reviewStorage.getReview(amelieReviewId).getUseful());
                    assertEquals(expectedReviewUseful, reviewStorage.getReview(batmanReviewId).getUseful());
                    assertEquals(expectedReviewUseful, reviewStorage.getReview(carrieReviewId).getUseful());
                };

        checkUserRelatedData.accept(false);

        userStorage.deleteUser(userId);

        checkUserRelatedData.accept(true);
    }
}