package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorageTestHelper;
import ru.yandex.practicum.filmorate.storage.UserStorageTestHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DBFilmStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final DBUserStorage userStorage;
    private final DBFilmStorage filmStorage;
    private final DBGenreStorage genreStorage;
    private final FilmService filmService;
    private final MpaService mpaService;

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
        Film film = filmStorageTestHelper.addFilm(1);

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
        LinkedHashSet<Genre> newSet = new LinkedHashSet<>();
        newSet.add(genreStorage.getGenreById(1));
        newSet.add(genreStorage.getGenreById(2));
        newSet.add(genreStorage.getGenreById(3));
        Film film = new Film(1,
                "Test film","test", LocalDate.now(),100,4,
                mpaService.getMpa(String.valueOf(1)),newSet,new LinkedHashSet<>(),new ArrayList<>());
       filmStorage.addFilm(film);
        int filmId = film.getId();
        filmStorage.getFilm(filmId);
        Integer genreSizeFilm = genreStorage.getGenresByFilmId(filmId).size();
        assertEquals(3, genreSizeFilm);

        filmStorage.deleteFilm(filmId);
        Supplier<Integer> filmGenresCountUpd =
                () -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM GENRELINE WHERE FILMID=?;",
                        Integer.class,
                        filmId
                );
        assertEquals(0, filmGenresCountUpd.get());
    }
}