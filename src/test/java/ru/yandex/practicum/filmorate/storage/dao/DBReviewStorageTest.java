package ru.yandex.practicum.filmorate.storage.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.*;


import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
class DBReviewStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewStorage reviewStorage;
    private final UserStorageTestHelper userStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;

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

        Review review1 = reviewStorage.addReview(review);

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

        reviewStorage.addReview(review);

        Review review1 = reviewStorage.getReview(1);

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

        reviewStorage.addReview(review);

        Review reviewEdited = reviewStorage.editReview(reviewNew);

        assertThat(reviewEdited).hasFieldOrPropertyWithValue("content", reviewNew.getContent());




    }

    @Test
    void removeReview() {
    }

    @Test
    void addLike() {
        final int userId = userStorageTestHelper.getNewUserId();
        final int filmId = filmStorageTestHelper.getNewFilmId();

        Review review = Review.builder().
                content("This movie was OSOM AS F").
                userId(userId).
                filmId(filmId).
                isPositive(true).
                build();

        reviewStorage.addReview(review);
        reviewStorage.addLike(1);

        assertThat(reviewStorage.getReview(1)).hasFieldOrPropertyWithValue("useful", 1);


    }

    @Test
    void shouldRemoveLike() {
        addLike();
        assertThat(reviewStorage.getReview(1)).hasFieldOrPropertyWithValue("useful", 1);
        reviewStorage.removeLike(1);
        assertThat(reviewStorage.getReview(1)).hasFieldOrPropertyWithValue("useful", 0);

    }
}