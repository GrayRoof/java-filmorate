package ru.yandex.practicum.filmorate.storage.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class DBFilmStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorageTestHelper userStorageTestHelper;
    private final DirectorStorageTestHelper directorStorageTestHelper;
    private final ReviewStorageTestHelper reviewStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;

    @Autowired
    public DBFilmStorageTest(
            JdbcTemplate jdbcTemplate,
            UserStorage userStorage,
            DirectorStorage directorStorage,
            ReviewStorage reviewStorage,
            DBFilmStorage filmStorage
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = filmStorage;

        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.directorStorageTestHelper = new DirectorStorageTestHelper(directorStorage);
        this.reviewStorageTestHelper = new ReviewStorageTestHelper(reviewStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM likes;");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("DELETE FROM genreline;");
        jdbcTemplate.update("DELETE FROM directorline;");
        jdbcTemplate.update("DELETE FROM directors;");
        jdbcTemplate.update("DELETE FROM film;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN userid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN directorid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN filmid RESTART WITH 1;");
    }

    @Test
    public void testGetFilmById() {
        final int filmId = filmStorageTestHelper.getNewFilmId();

        Film dbFilm = filmStorage.getFilm(filmId);

        assertThat(dbFilm).hasFieldOrPropertyWithValue("id", filmId);
    }

    @Test
    void getAllFilms() {
        filmStorageTestHelper.getNewFilmId();
        filmStorageTestHelper.getNewFilmId();

        Collection<Film> dbFilms = filmStorage.getAllFilms();

        assertEquals(2, dbFilms.size());
    }

    @Test
    void updateFilm() {
        Film film = filmStorageTestHelper.addFilm(1, List.of(1),List.of());

        film.setName("update");
        filmStorage.updateFilm(film);

        Film dbFilm = filmStorage.getFilm(film.getId());
        assertThat(dbFilm).hasFieldOrPropertyWithValue("name", "update");
    }

    @Test
    void deleteFilm() {
        final int amelieId = filmStorageTestHelper.getNewFilmId();
        final int batmanId = filmStorageTestHelper.getNewFilmId();
        assertTrue(filmStorage.containsFilm(amelieId));
        assertTrue(filmStorage.containsFilm(batmanId));

        filmStorage.deleteFilm(amelieId);

        assertFalse(filmStorage.containsFilm(amelieId));
        assertTrue(filmStorage.containsFilm(batmanId));
    }

    @Test
    @Tag(DBTestTags.DB_LOW_LEVEL)
    void deleteFilmDeletesLikes() {
        final int filmId = filmStorageTestHelper.getNewFilmId();

        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();
        filmStorage.addLike(filmId, annId);
        filmStorage.addLike(filmId, bobId);
        filmStorage.addLike(filmId, camId);

        Supplier<Integer> filmLikesCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM likes WHERE filmid=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(3, filmLikesCount.get());

        filmStorage.deleteFilm(filmId);

        assertEquals(0, filmLikesCount.get());
    }

    @Test
    @Tag(DBTestTags.DB_LOW_LEVEL)
    void deleteFilmDeletesFilmGenres() {
        final int filmId = filmStorageTestHelper.addFilm(1, List.of(1, 2, 3),List.of()).getId();

        Supplier<Integer> filmGenresCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genreline WHERE filmid=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(3, filmGenresCount.get());

        filmStorage.deleteFilm(filmId);

        assertEquals(0, filmGenresCount.get());
    }

    @Test
    @Tag(DBTestTags.DB_LOW_LEVEL)
    void deleteFilmDeletesFilmDirectors() {
        final int allenId = directorStorageTestHelper.getNewDirectorId();
        final int brassId = directorStorageTestHelper.getNewDirectorId();
        final int cohenId = directorStorageTestHelper.getNewDirectorId();

        final int filmId = filmStorageTestHelper.addFilm(
                1,
                List.of(),
                List.of(allenId, brassId, cohenId)
        ).getId();

        Supplier<Integer> filmDirectorsCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM directorline WHERE filmid=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(3, filmDirectorsCount.get());

        filmStorage.deleteFilm(filmId);

        assertEquals(0, filmDirectorsCount.get());
    }

    @Test
    @Tag(DBTestTags.DB_LOW_LEVEL)
    void deleteFilmDeletesReviews() {
        final int filmId = filmStorageTestHelper.getNewFilmId();

        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();

        reviewStorageTestHelper.addReview(filmId, annId, true);
        reviewStorageTestHelper.addReview(filmId, bobId, true);
        reviewStorageTestHelper.addReview(filmId, camId, true);

        Supplier<Integer> filmReviewsCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reviews WHERE filmid=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(3, filmReviewsCount.get());

        filmStorage.deleteFilm(filmId);

        assertEquals(0, filmReviewsCount.get());
    }

    @Test
    void getCommonFilms() {
        final int annId = userStorageTestHelper.getNewUserId();
        final int bobId = userStorageTestHelper.getNewUserId();
        final int camId = userStorageTestHelper.getNewUserId();

        final int amelieId = filmStorageTestHelper.getNewFilmId();
        final int batmanId = filmStorageTestHelper.getNewFilmId();
        final int carrieId = filmStorageTestHelper.getNewFilmId();
        final int djangoId = filmStorageTestHelper.getNewFilmId();

        filmStorage.addLike(amelieId, annId);

        //common
        filmStorage.addLike(batmanId, annId);
        filmStorage.addLike(batmanId, bobId);

        //common, top-rated
        filmStorage.addLike(carrieId, annId);
        filmStorage.addLike(carrieId, bobId);
        filmStorage.addLike(carrieId, camId);

        filmStorage.addLike(djangoId, bobId);

        List<Film> result = new ArrayList<>(filmStorage.getCommonFilms(annId, bobId));

        assertEquals(2, result.size());
        assertEquals(carrieId, result.get(0).getId());
        assertEquals(batmanId, result.get(1).getId());
    }
}