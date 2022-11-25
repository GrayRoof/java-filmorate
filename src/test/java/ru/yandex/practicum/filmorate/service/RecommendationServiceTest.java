package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.storage.dao.DBTestQueryConstants.SQL_PREPARE_DB;

@SpringBootTest
@AutoConfigureTestDatabase
public class RecommendationServiceTest {

    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final RecommendationService recommendationService;
    private final UserStorageTestHelper userStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;

    @Autowired
    public RecommendationServiceTest(
            JdbcTemplate jdbcTemplate,
            UserStorage userStorage,
            FilmStorage filmStorage,
            RecommendationService recommendationService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = filmStorage;
        this.recommendationService = recommendationService;

        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(SQL_PREPARE_DB);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM likes;");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("DELETE FROM genreline;");
        jdbcTemplate.update("DELETE FROM film;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN userid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN filmid RESTART WITH 1;");
    }

    @Test
    public void getRecommendationsIfNoLikes() {
        final int newUserId = userStorageTestHelper.getNewUserId();

        Collection<Film> dbFilms = recommendationService.getRecommendations(String.valueOf(newUserId));
        assertEquals(0, dbFilms.size());
    }

    @Test
    public void getRecommendationsIfNoSameLikes() {
        final int newUserId = userStorageTestHelper.getNewUserId();
        final int filmId = filmStorageTestHelper.getNewFilmId();
        filmStorage.addLike(filmId, newUserId);

        Collection<Film> dbFilms = recommendationService.getRecommendations(String.valueOf(newUserId));
        assertEquals(0, dbFilms.size());
    }

    @Test
    public void getRecommendationsIfOnlySameLikes() {
        final int newUserId1 = userStorageTestHelper.getNewUserId();
        final int newUserId2 = userStorageTestHelper.getNewUserId();
        final int filmId = filmStorageTestHelper.getNewFilmId();
        filmStorage.addLike(filmId, newUserId1);
        filmStorage.addLike(filmId, newUserId2);

        Collection<Film> dbFilms = recommendationService.getRecommendations(String.valueOf(newUserId1));
        assertEquals(0, dbFilms.size());
        dbFilms = recommendationService.getRecommendations(String.valueOf(newUserId2));
        assertEquals(0, dbFilms.size());
    }

    @Test
    public void getRecommendationsToUser1FromUser2() {
        final int newUserId1 = userStorageTestHelper.getNewUserId();
        final int newUserId2 = userStorageTestHelper.getNewUserId();
        final int filmId1 = filmStorageTestHelper.getNewFilmId();
        final int filmId2 = filmStorageTestHelper.getNewFilmId();
        final int filmId3 = filmStorageTestHelper.getNewFilmId();
        filmStorage.addLike(filmId3, newUserId1);
        filmStorage.addLike(filmId3, newUserId2);
        filmStorage.addLike(filmId1, newUserId1);
        filmStorage.addLike(filmId2, newUserId2);

        Collection<Film> dbFilms = recommendationService.getRecommendations(String.valueOf(newUserId1));
        assertEquals(1, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm2 = filmStorage.getFilm(filmId2);
        assertEquals(film.getId(), testFilm2.getId());
        assertEquals(film.getName(), testFilm2.getName());
        assertEquals(film.getDescription(), testFilm2.getDescription());
        assertEquals(film.getReleaseDate(), testFilm2.getReleaseDate());
        assertEquals(film.getDuration(), testFilm2.getDuration());
        assertEquals(film.getRate(), testFilm2.getRate());
        assertEquals(film.getMpa(), testFilm2.getMpa());
        assertEquals(film.getGenres(), testFilm2.getGenres());
    }

    @Test
    public void getRecommendationsToUser2FromUser1() {
        final int newUserId1 = userStorageTestHelper.getNewUserId();
        final int newUserId2 = userStorageTestHelper.getNewUserId();
        final int filmId1 = filmStorageTestHelper.getNewFilmId();
        final int filmId2 = filmStorageTestHelper.getNewFilmId();
        final int filmId3 = filmStorageTestHelper.getNewFilmId();
        filmStorage.addLike(filmId3, newUserId1);
        filmStorage.addLike(filmId3, newUserId2);
        filmStorage.addLike(filmId1, newUserId1);
        filmStorage.addLike(filmId2, newUserId2);

        Collection<Film> dbFilms = recommendationService.getRecommendations(String.valueOf(newUserId2));
        assertEquals(1, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm1 = filmStorage.getFilm(filmId1);
        assertEquals(film.getId(), testFilm1.getId());
        assertEquals(film.getName(), testFilm1.getName());
        assertEquals(film.getDescription(), testFilm1.getDescription());
        assertEquals(film.getReleaseDate(), testFilm1.getReleaseDate());
        assertEquals(film.getDuration(), testFilm1.getDuration());
        assertEquals(film.getRate(), testFilm1.getRate());
        assertEquals(film.getMpa(), testFilm1.getMpa());
        assertEquals(film.getGenres(), testFilm1.getGenres());
    }

}
