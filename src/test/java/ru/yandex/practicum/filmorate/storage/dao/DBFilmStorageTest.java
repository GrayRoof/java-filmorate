package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorageTestHelper;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorageTestHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DBFilmStorageTest {

    private final JdbcTemplate jdbcTemplate;
    @Qualifier(DBStorageConsts.QUALIFIER)
    private final UserStorage userStorage;
    private final DBFilmStorage filmStorage;

    private UserStorageTestHelper userStorageTestHelper;
    private FilmStorageTestHelper filmStorageTestHelper;

    @BeforeEach
    void beforeEach() {
        userStorageTestHelper = new UserStorageTestHelper(userStorage);
        filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
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
    void deleteFilmDeletesFilmGenres() {
        final int filmId = filmStorageTestHelper.addFilm(1, List.of(1, 2, 3),List.of()).getId();

        Supplier<Integer> filmGenresCount =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM GENRELINE WHERE filmid=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(3, filmGenresCount.get());

        filmStorage.deleteFilm(filmId);

        assertEquals(0, filmGenresCount.get());
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