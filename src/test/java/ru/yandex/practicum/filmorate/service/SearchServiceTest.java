package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
public class SearchServiceTest {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorageTestHelper userStorageTestHelper;
    private final FilmStorageTestHelper filmStorageTestHelper;
    private final FilmStorage filmStorage;
    private final DirectorService directorService;
    private final DirectorStorage directorStorage;
    private final SearchService searchService;

    private Film filmWith2Like2Director;
    private Film filmWith1Like1Director;
    private Film filmWith0Like1Director;
    private Film filmWith2Like0Director;

    @Autowired
    public SearchServiceTest(JdbcTemplate jdbcTemplate,
                             @Qualifier(UsedStorageConsts.QUALIFIER) FilmStorage filmStorage,
                             @Qualifier(UsedStorageConsts.QUALIFIER) UserStorage userStorage,
                             DirectorService directorService,
                             @Qualifier(UsedStorageConsts.QUALIFIER) DirectorStorage directorStorage, SearchService searchService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorageTestHelper = new UserStorageTestHelper(userStorage);
        this.filmStorageTestHelper = new FilmStorageTestHelper(filmStorage);
        this.filmStorage = filmStorage;
        this.directorService = directorService;
        this.directorStorage = directorStorage;
        this.searchService = searchService;
    }

    @BeforeEach
    void setUp() {
        initializingTestData();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM likes;");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("DELETE FROM directors;");
        jdbcTemplate.update("DELETE FROM directorline;");
        jdbcTemplate.update("DELETE FROM film;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN userid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN filmid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN directorid RESTART WITH 1;");
    }

    @Test
    public void searchFilmsAnywayRegister() {
        Collection<Film> dbFilms = searchService.filmSearch("TwO", new HashSet<>(Arrays.asList("director", "title")));

        assertEquals(3, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm3 = filmStorage.get(filmWith2Like2Director.getId());
        assertEquals(film.getId(), testFilm3.getId());
        assertEquals(film.getName(), testFilm3.getName());
        assertEquals(film.getDescription(), testFilm3.getDescription());
        assertEquals(film.getReleaseDate(), testFilm3.getReleaseDate());
        assertEquals(film.getDuration(), testFilm3.getDuration());
        assertEquals(film.getRate(), testFilm3.getRate());
    }

    @Test
    public void searchFilmsAnywayEmpty() {
        Collection<Film> dbFilms = searchService.filmSearch("Пусто", new HashSet<>(Arrays.asList("title", "director")));

        assertEquals(0, dbFilms.size());
    }

    @Test
    public void searchFilmsOnlyTitle() {
        Collection<Film> dbFilms = searchService.filmSearch("one", new HashSet<>(List.of("title")));

        assertEquals(2, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm3 = filmStorage.get(filmWith2Like2Director.getId());
        assertEquals(film.getId(), testFilm3.getId());
        assertEquals(film.getName(), testFilm3.getName());
        assertEquals(film.getDescription(), testFilm3.getDescription());
        assertEquals(film.getReleaseDate(), testFilm3.getReleaseDate());
        assertEquals(film.getDuration(), testFilm3.getDuration());
        assertEquals(film.getRate(), testFilm3.getRate());
    }

    @Test
    public void searchFilmsOnlyDirector() {
        Collection<Film> dbFilms = searchService.filmSearch("r One", new HashSet<>(List.of("director")));

        assertEquals(2, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm2 = filmStorage.get(filmWith2Like2Director.getId());
        assertEquals(film.getId(), testFilm2.getId());
        assertEquals(film.getName(), testFilm2.getName());
        assertEquals(film.getDescription(), testFilm2.getDescription());
        assertEquals(film.getReleaseDate(), testFilm2.getReleaseDate());
        assertEquals(film.getDuration(), testFilm2.getDuration());
        assertEquals(film.getRate(), testFilm2.getRate());
    }

    @Test
    public void searchFilmsWithOutDirector() {
        Collection<Film> dbFilms =
                searchService.filmSearch("Test name", new HashSet<>(Arrays.asList("director", "title")));

        assertEquals(1, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm2 = filmStorage.get(filmWith2Like0Director.getId());
        assertEquals(film.getId(), testFilm2.getId());
        assertEquals(film.getName(), testFilm2.getName());
        assertEquals(film.getDescription(), testFilm2.getDescription());
        assertEquals(film.getReleaseDate(), testFilm2.getReleaseDate());
        assertEquals(film.getDuration(), testFilm2.getDuration());
        assertEquals(film.getRate(), testFilm2.getRate());
    }

    @Test
    public void searchFilmsWithOutLike() {
        Collection<Film> dbFilms =
                searchService.filmSearch("Test one two name", new HashSet<>(Arrays.asList("director", "title")));

        assertEquals(1, dbFilms.size());
        Film film = dbFilms.iterator().next();
        Film testFilm2 = filmStorage.get(filmWith0Like1Director.getId());
        assertEquals(film.getId(), testFilm2.getId());
        assertEquals(film.getName(), testFilm2.getName());
        assertEquals(film.getDescription(), testFilm2.getDescription());
        assertEquals(film.getReleaseDate(), testFilm2.getReleaseDate());
        assertEquals(film.getDuration(), testFilm2.getDuration());
        assertEquals(film.getRate(), testFilm2.getRate());
    }

    private void initializingTestData() {
        final int newUserId1 = userStorageTestHelper.getNewUserId();
        final int newUserId2 = userStorageTestHelper.getNewUserId();

        final int filmId1 = filmStorageTestHelper.getNewFilmId();
        final int filmId2 = filmStorageTestHelper.getNewFilmId();
        final int filmId3 = filmStorageTestHelper.getNewFilmId();
        final int filmId4 = filmStorageTestHelper.getNewFilmId();

        filmStorage.addLike(filmId3, newUserId1);
        filmStorage.addLike(filmId3, newUserId2);
        filmStorage.addLike(filmId1, newUserId1);
        filmStorage.addLike(filmId4, newUserId2);

        Director director1 = new Director(1, "Director one");
        Director director2 = new Director(2, "Director two");
        directorService.addDirector(director1);
        directorService.addDirector(director2);

        LinkedHashSet directors = new LinkedHashSet();

        filmWith2Like2Director = filmStorage.get(filmId3);
        directors.add(director1);
        directors.add(director2);
        filmWith2Like2Director.setDirectors(directors);
        filmWith2Like2Director.setName("Test One name");
        filmStorage.update(filmWith2Like2Director);
        filmWith2Like2Director = filmStorage.get(filmId3);
        directorStorage.load(List.of(filmWith2Like2Director));

        filmWith1Like1Director = filmStorage.get(filmId1);
        directors.clear();
        directors.add(director1);
        filmWith1Like1Director.setDirectors(directors);
        filmWith1Like1Director.setName("Test tWo name");
        filmStorage.update(filmWith1Like1Director);
        filmWith1Like1Director = filmStorage.get(filmId1);
        directorStorage.load(List.of(filmWith1Like1Director));

        filmWith0Like1Director = filmStorage.get(filmId2);
        directors.clear();
        directors.add(director2);
        filmWith0Like1Director.setDirectors(directors);
        filmWith0Like1Director.setName("Test ONE tWo name");
        filmStorage.update(filmWith0Like1Director);
        filmWith0Like1Director = filmStorage.get(filmId2);
        directorStorage.load(List.of(filmWith0Like1Director));

        filmWith2Like0Director = filmStorage.get(filmId4);
        filmWith2Like0Director.setName("TEST name");
        filmStorage.update(filmWith2Like0Director);
        filmWith2Like0Director = filmStorage.get(filmId4);
        directorStorage.load(List.of(filmWith2Like0Director));
    }
}
