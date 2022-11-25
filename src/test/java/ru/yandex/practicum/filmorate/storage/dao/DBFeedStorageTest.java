package ru.yandex.practicum.filmorate.storage.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.*;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.storage.dao.DBTestQueryConstants.SQL_PREPARE_DB;

@SpringBootTest
@AutoConfigureTestDatabase
class DBFeedStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewStorage reviewStorage;
    private final FeedStorage feedStorage;
    private final UserStorageTestHelper userStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;

    @Autowired
    DBFeedStorageTest(JdbcTemplate jdbcTemplate,
                      UserStorage userStorage,
                      FilmStorage filmStorage,
                      ReviewStorage reviewStorage,
                      FeedStorage feedStorage) {

        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.reviewStorage = reviewStorage;
        this.feedStorage = feedStorage;

        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update(SQL_PREPARE_DB);
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
        Review review = Review.builder()
                .reviewId(0)
                .content("content")
                .isPositive(true)
                .userId(secondUserId)
                .filmId(filmId)
                .build();

        final int reviewId = reviewStorage.addReview(review).getReviewId();

        FeedEvent[] actualFirst = feedStorage.getFeed(firstUserId).toArray(FeedEvent[]::new);
        assertEquals(2, actualFirst.length,
                "Количество объектов в ленте firstUser не совпадает с ожидаемым");
        assertEquals(filmId, actualFirst[0].getEntityId(),
                "id сущности для первого события ленты firstUser не тот");
        assertEquals(1, actualFirst[0].getEventId(),
                "id первого события ленты firstUser не тот");
        assertEquals("LIKE", actualFirst[0].getEventType().toString(),
                "Неверный тип первого события ленты firstUser");
        assertEquals("ADD", actualFirst[0].getOperation().toString(),
                "Неверная операция первого события ленты firstUser");

        assertEquals(secondUserId, actualFirst[1].getEntityId(),
                "id сущности для второго события ленты firstUser не тот");
        assertEquals(3, actualFirst[1].getEventId(),
                "id второго события ленты firstUser не тот");
        assertEquals("FRIEND", actualFirst[1].getEventType().toString(),
                "Неверный тип второго события ленты firstUser");
        assertEquals("ADD", actualFirst[1].getOperation().toString(),
                "Неверная операция второго события ленты firstUser");

        FeedEvent[] actualSecond = feedStorage.getFeed(secondUserId).toArray(FeedEvent[]::new);
        assertEquals(1, actualSecond.length,
                "Количество объектов в ленте secondUserId не совпадает с ожидаемым");
        assertEquals(reviewId, actualSecond[0].getEntityId(),
                "id сущности для первого события ленты secondUserId не тот");
        assertEquals(4, actualSecond[0].getEventId(),
                "id первого события ленты secondUserId не тот");
        assertEquals("REVIEW", actualSecond[0].getEventType().toString(),
                "Неверный тип первого события ленты secondUserId");
        assertEquals("ADD", actualSecond[0].getOperation().toString(),
                "Неверная операция первого события ленты secondUserId");

        FeedEvent[] actualThird = feedStorage.getFeed(thirdUserId).toArray(FeedEvent[]::new);
        assertEquals(1, actualThird.length,
                "Количество объектов в ленте thirdUserId не совпадает с ожидаемым");
        assertEquals(firstUserId, actualThird[0].getEntityId(),
                "id сущности для первого события ленты thirdUserId не тот");
        assertEquals(2, actualThird[0].getEventId(),
                "id первого события ленты thirdUserId не тот");
        assertEquals("FRIEND", actualThird[0].getEventType().toString(),
                "Неверный тип первого события ленты thirdUserId");
        assertEquals("ADD", actualThird[0].getOperation().toString(),
                "Неверная операция первого события ленты thirdUserId");


    }
}