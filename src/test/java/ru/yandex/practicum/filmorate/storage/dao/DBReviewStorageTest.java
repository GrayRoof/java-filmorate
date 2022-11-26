package ru.yandex.practicum.filmorate.storage.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.*;


import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.storage.dao.DBTestQueryConstants.SQL_PREPARE_DB;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase
class DBReviewStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewStorage reviewStorage;
    private final UserStorageTestHelper userStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;
    private final ReviewStorageTestHelper reviewStorageTestHelper;

    @Autowired
    public DBReviewStorageTest(
            JdbcTemplate jdbcTemplate,
            UserStorage userStorage,
            FilmStorage filmStorage,
            DBReviewStorage reviewStorage
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewStorage = reviewStorage;

        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
        this.reviewStorageTestHelper = new ReviewStorageTestHelper(reviewStorage);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(SQL_PREPARE_DB);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM friendship;");
        jdbcTemplate.update("DELETE FROM likes;");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("DELETE FROM film;");
        jdbcTemplate.update("DELETE FROM reviews;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN userid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN filmid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN reviewid RESTART WITH 1;");
    }


    @Test
    void shouldAddReview() {
        final int userId = userStorageTestHelper.getNewUserId();
        final int filmId = filmStorageTestHelper.getNewFilmId();

        Review review = Review.builder().
                content("This movie was OSOM AS F").
                userId(userId).
                filmId(filmId).
                isPositive(true).
                build();

        Review review1 = reviewStorage.add(review);

        assertThat(review1).hasFieldOrPropertyWithValue("reviewId", 1);
        assertThat(review1).hasFieldOrPropertyWithValue("content", review.getContent());
        assertThat(review1).hasFieldOrPropertyWithValue("userId", review.getUserId());
        assertThat(review1).hasFieldOrPropertyWithValue("filmId", review.getFilmId());
        assertThat(review1).hasFieldOrPropertyWithValue("isPositive", review.getIsPositive());

    }

    @Test
    void shouldGetReview() {
        final int userId = userStorageTestHelper.getNewUserId();
        final int filmId = filmStorageTestHelper.getNewFilmId();

        Review review = Review.builder().
                content("This movie was OSOM AS F").
                userId(userId).
                filmId(filmId).
                isPositive(true).
                build();

        reviewStorage.add(review);

        Review review1 = reviewStorage.get(1);

        assertThat(review1).hasFieldOrPropertyWithValue("reviewId", 1);
    }

    @Test
    void shouldGetEditedReview() {
        final int userId = userStorageTestHelper.getNewUserId();
        final int filmId = filmStorageTestHelper.getNewFilmId();

        Review review = Review.builder().
                content("This movie was OSOM AS F").
                userId(userId).
                filmId(filmId).
                isPositive(true).
                build();

        Review reviewNew = Review.builder().
                reviewId(1).
                content("This movie was AWESOME").
                userId(userId).
                filmId(filmId).
                isPositive(true).
                build();

        reviewStorage.add(review);

        Review reviewEdited = reviewStorage.update(reviewNew);

        assertThat(reviewEdited).hasFieldOrPropertyWithValue("content", reviewNew.getContent());




    }

    @Test
    void removeReview() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int annReviewId = reviewStorageTestHelper.getNewReviewId(filmId, annId, true);
        final int bobReviewId = reviewStorageTestHelper.getNewReviewId(filmId, bobId, true);

        assertTrue(reviewStorage.contains(annReviewId));
        assertTrue(reviewStorage.contains(bobReviewId));

        reviewStorage.delete(bobReviewId + "" /*TODO: refactor removeReview, there must be int arg here!*/);

        assertTrue(reviewStorage.contains(annReviewId));
        assertFalse(reviewStorage.contains(bobReviewId));
    }

    @Test
    @Tag(DBTestTags.DB_LOW_LEVEL)
    void removeReviewRemovesScores() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);

        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();

        reviewStorage.setScoreFromUser(reviewId, annId, true);
        reviewStorage.setScoreFromUser(reviewId, bobId, true);
        reviewStorage.setScoreFromUser(reviewId, camId, true);

        Supplier<Integer> reviewScoresCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM useful WHERE reviewid=?;",
                        Integer.class,
                        reviewId
                );
        assertEquals(3, reviewScoresCount.get());

        reviewStorage.delete(reviewId + "" /*TODO: refactor removeReview, there must be int arg here!*/);

        assertEquals(0, reviewScoresCount.get());
    }

    @Test
    void shouldSetAbsentPositiveScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        assertTrue(reviewStorage.getScoreFromUser(reviewId, userId).isEmpty());

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.setScoreFromUser(reviewId, userId, true);

        assertTrue(reviewStorage.getScoreFromUser(reviewId, userId).get());
        assertEquals(prevUsefulness + 1, reviewUsefulness.get());
    }

    @Test
    void shouldNotSetExistentPositiveScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        reviewStorage.setScoreFromUser(reviewId, userId, true);

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.setScoreFromUser(reviewId, userId, true);

        assertTrue(reviewStorage.getScoreFromUser(reviewId, userId).get());
        assertEquals(prevUsefulness, reviewUsefulness.get());
    }

    @Test
    void shouldNotUnsetAbsentPositiveScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.unsetScoreFromUser(reviewId, userId, true);

        assertEquals(prevUsefulness, reviewUsefulness.get());
    }

    @Test
    void shouldUnsetPositiveScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        reviewStorage.setScoreFromUser(reviewId, userId, true);

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.unsetScoreFromUser(reviewId, userId, true);

        assertTrue(reviewStorage.getScoreFromUser(reviewId, userId).isEmpty());
        assertEquals(prevUsefulness - 1, reviewUsefulness.get());
    }

    @Test
    void shouldSetAbsentNegativeScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        assertTrue(reviewStorage.getScoreFromUser(reviewId, userId).isEmpty());

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.setScoreFromUser(reviewId, userId, false);

        assertFalse(reviewStorage.getScoreFromUser(reviewId, userId).get());
        assertEquals(prevUsefulness - 1, reviewUsefulness.get());
    }

    @Test
    void shouldNotSetExistentNegativeScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        reviewStorage.setScoreFromUser(reviewId, userId, false);

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.setScoreFromUser(reviewId, userId, false);

        assertFalse(reviewStorage.getScoreFromUser(reviewId, userId).get());
        assertEquals(prevUsefulness, reviewUsefulness.get());
    }

    @Test
    void shouldNotUnsetAbsentNegativeScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.unsetScoreFromUser(reviewId, userId, false);

        assertEquals(prevUsefulness, reviewUsefulness.get());
    }

    @Test
    void shouldUnsetNegativeScore() {
        final int filmId = filmStorageTestHelper.getNewFilmId();
        final int authorId = userStorageTestHelper.getNewUserId();
        final int reviewId = reviewStorageTestHelper.getNewReviewId(filmId, authorId, true);
        final int userId = userStorageTestHelper.getNewUserId();

        Supplier<Integer> reviewUsefulness =
                () -> reviewStorage.get(reviewId).getUseful();

        reviewStorage.setScoreFromUser(reviewId, userId, false);

        int prevUsefulness = reviewUsefulness.get();
        reviewStorage.unsetScoreFromUser(reviewId, userId, false);

        assertTrue(reviewStorage.getScoreFromUser(reviewId, userId).isEmpty());
        assertEquals(prevUsefulness + 1, reviewUsefulness.get());
    }

}