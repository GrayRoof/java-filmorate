package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.storage.dao.DBTestQueryConstants.SQL_PREPARE_DB;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class FilmServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    FilmService service;
    @Autowired
    DirectorService directorService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(SQL_PREPARE_DB);
    }

    @Autowired
    GenreStorage genreStorage;

    @Test
    void shouldAddWhenAddValidFilmData() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmNameEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("???????????? ?????????????????? ????????????: " +
                "?????? ???????????? ?????????????????? ?????????????????? ??????????????. ", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmNameBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("???????????? ?????????????????? ????????????: " +
                "?????? ???????????? ?????????????????? ?????????????????? ??????????????. ", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmDuration() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(-100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("???????????? ?????????????????? ????????????: " +
                "?????????????????????????????????? ???????????? ???? ?????????? ???????? ??????????????????????????. ", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmReleaseDate() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("???????????? ?????????????????? ????????????: " +
                "???????? ???????????? ???? ?????????? ???????? ???????????? 28 ?????????????? 1895??.", ex.getMessage());
    }

    @Test
    void shouldAddWhenAddValidFilmReleaseDateBoundary() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmDescription() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Failed description. Failed description. Failed description. Failed description. " +
                "Failed description. Failed description. Failed description. Failed description. " +
                "Failed description. Failed description. F");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("???????????? ?????????????????? ????????????: " +
                "???????????????? ???????????? ???? ???????????? ?????????????????? 200 ????????????????. ", ex.getMessage());
    }

    @Test
    void shouldAddWhenAddFilmDescriptionBoundary() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description. Correct description. Correct description. Correct description. " +
                "Correct description. Correct description. Correct description. Correct description. " +
                "Correct description. Correct des");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenUpdateFailedFilmId() {
        Film film = new Film();
        film.setId(999);
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(film));
        assertEquals("?????????? ?? ?????????????????????????????? 999 ???? ??????????????????????????????!", ex.getMessage());
    }


    @Test
    void shouldAddWhenAddValidFilmDataWithDirector() {
        Director director = directorService.addDirector(new Director(1, "Test Dir"));
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        film.getDirectors().add(director);
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
        assertTrue(film.getDirectors().contains(director));
    }

    @Test
    void shouldUpdateWhenAddValidFilmDataWithDirector() {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        Film addedFilm = service.add(film);
        Director director = directorService.addDirector(new Director(1, "Test Dir"));
        Film film2 = new Film();
        film2.setId(addedFilm.getId());
        film2.setName("Correct Name");
        film2.setDescription("Correct description.");
        film2.setReleaseDate(LocalDate.of(1995, 5, 26));
        film2.setDuration(100L);
        film2.setRate(0);
        film2.setMpa(new Mpa(1, "mpa", "description"));
        film2.getDirectors().add(director);
        service.update(film2);
        Film filmCheck = service.get(String.valueOf(film.getId()));
        assertTrue(filmCheck.getDirectors().contains(director));
    }

    @Test
    void shouldUpdateWhenAddValidFilmDataWithDeleteDirector() {
        Director director = directorService.addDirector(new Director(1, "Test Dir"));
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        film.getDirectors().add(director);
        Film addedFilm = service.add(film);
        Film film2 = new Film();
        film2.setId(addedFilm.getId());
        film2.setName("Correct Name");
        film2.setDescription("Correct description.");
        film2.setReleaseDate(LocalDate.of(1995, 5, 26));
        film2.setDuration(100L);
        film2.setRate(0);
        film2.setMpa(new Mpa(1, "mpa", "description"));
        service.update(film2);
        assertFalse(service.get(String.valueOf(1)).getDirectors().contains(director));
    }

    @Test
    void shouldGetSortByYearFilmWithDirector() {
        Director director = directorService.addDirector(new Director(1, "Test Dir"));
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        film.getDirectors().add(director);
        Film addedFilm = service.add(film);
        Film film2 = new Film();
        film2.setName("Film2");
        film2.setDescription("Correct description.");
        film2.setReleaseDate(LocalDate.of(1985, 5, 26));
        film2.setDuration(100L);
        film2.setRate(0);
        film2.setMpa(new Mpa(1, "mpa", "description"));
        film2.getDirectors().add(director);
        Film addedFilm2 = service.add(film2);
        ArrayList<Film> sortedFilm = new ArrayList<>(service.getSortedFilmWithDirector(1, "year"));
        assertEquals(addedFilm2, sortedFilm.get(0));
    }

    @Test
    void shouldGetSortByLikesFilmWithDirector() {
        User user = new User(0,
                "correct.email@mail.ru",
                "correctlogin",
                "Correct Name",
                LocalDate.of(2002, 1, 1),
                new ArrayList<>());
        User addedUser = userService.add(user);
        Director director = directorService.addDirector(new Director(1, "Test Dir"));
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        film.getDirectors().add(director);
        Film addedFilm = service.add(film);
        Film film2 = new Film();
        film2.setName("Film2");
        film2.setDescription("Correct description.");
        film2.setReleaseDate(LocalDate.of(1985, 5, 26));
        film2.setDuration(100L);
        film2.setRate(0);
        film2.setMpa(new Mpa(1, "mpa", "description"));
        film2.getDirectors().add(director);
        Film addedFilm2 = service.add(film2);
        service.addLike(String.valueOf(addedFilm2.getId()), String.valueOf(addedUser.getId()));
        ArrayList<Film> sortedFilm = new ArrayList<>(service.getSortedFilmWithDirector(1, "likes"));
        assertEquals(addedFilm2, sortedFilm.get(0));
    }

    @Test
    void shouldThrowGetSortByLikesFilmWithoutDirector() {
        User user = new User(0,
                "correct.email@mail.ru",
                "correctlogin",
                "Correct Name",
                LocalDate.of(2002, 1, 1),
                new ArrayList<>());
        User addedUser = userService.add(user);
        Director director = directorService.addDirector(new Director(1, "Test Dir"));
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setDuration(100L);
        film.setRate(0);
        film.setMpa(new Mpa(1, "mpa", "description"));
        film.getDirectors().add(director);
        Film addedFilm = service.add(film);
        Film film2 = new Film();
        film2.setName("Film2");
        film2.setDescription("Correct description.");
        film2.setReleaseDate(LocalDate.of(1985, 5, 26));
        film2.setDuration(100L);
        film2.setRate(0);
        film2.setMpa(new Mpa(1, "mpa", "description"));
        film2.getDirectors().add(director);
        Film addedFilm2 = service.add(film);
        service.addLike(String.valueOf(addedFilm2.getId()), String.valueOf(addedUser.getId()));
        assertThrows(NotFoundException.class, () -> service.getSortedFilmWithDirector(2, "likes"));
    }

    @Test
    void shouldReturnCollectionOfFilmsByGenreWithoutYear(){
        Genre genre1 = genreStorage.getById(1);
        Genre genre2 = genreStorage.getById(2);
        LinkedHashSet<Genre> genres1 = new LinkedHashSet<>();
        LinkedHashSet<Genre> genres2 = new LinkedHashSet<>();

        genres1.add(genre1);
        genres2.add(genre1);
        genres2.add(genre2);

        Film film = new Film();
        film.setId(999);
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995, 5, 26));
        film.setGenres(genres1);
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));

        Film film2 = new Film();
        film2.setId(998);
        film2.setName("Peekaboo");
        film2.setDescription("Description.");
        film2.setReleaseDate(LocalDate.of(1990, 5, 26));
        film2.setDuration(100L);
        film2.setGenres(genres2);
        film2.setMpa(new Mpa(1, "mpa", "description"));

        service.add(film);
        service.add(film2);

        Collection<Film> filmsWithGenres = service.getMostPopular("10", "1", "all");

        assertEquals(2, filmsWithGenres.size());


    }


    @Test
    void shouldReturnCollectionOfFilmsByYearWithoutGenre(){
        Genre genre1 = genreStorage.getById(1);
        Genre genre2 = genreStorage.getById(2);
        LinkedHashSet<Genre> genres1 = new LinkedHashSet<>();
        LinkedHashSet<Genre> genres2 = new LinkedHashSet<>();

        genres1.add(genre1);
        genres2.add(genre1);
        genres2.add(genre2);

        Film film = new Film();
        film.setId(999);
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1939, 5, 26));
        film.setGenres(genres1);
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));

        Film film2 = new Film();
        film2.setId(998);
        film2.setName("Peekaboo");
        film2.setDescription("Description.");
        film2.setReleaseDate(LocalDate.of(1990, 5, 26));
        film2.setDuration(100L);
        film2.setGenres(genres2);
        film2.setMpa(new Mpa(1, "mpa", "description"));

        service.add(film);
        service.add(film2);

        Collection<Film> filmsWithYear = service.getMostPopular("10", "all", "1939");

        assertEquals(1, filmsWithYear.size());


    }

    @Test
    void shouldReturnCollectionOfFilmsByYearWithGenre(){
        Genre genre1 = genreStorage.getById(1);
        Genre genre2 = genreStorage.getById(2);
        LinkedHashSet<Genre> genres1 = new LinkedHashSet<>();
        LinkedHashSet<Genre> genres2 = new LinkedHashSet<>();

        genres1.add(genre1);
        genres2.add(genre1);
        genres2.add(genre2);

        Film film = new Film();
        film.setId(999);
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1939, 5, 26));
        film.setGenres(genres1);
        film.setDuration(100L);
        film.setMpa(new Mpa(1, "mpa", "description"));

        Film film2 = new Film();
        film2.setId(998);
        film2.setName("Peekaboo");
        film2.setDescription("Description.");
        film2.setReleaseDate(LocalDate.of(1990, 5, 26));
        film2.setDuration(100L);
        film2.setGenres(genres2);
        film2.setMpa(new Mpa(1, "mpa", "description"));

        service.add(film);
        service.add(film2);

        Collection<Film> filmsWithGenresAndYear = service.getMostPopular("10", "1", "1939");

        assertEquals(1, filmsWithGenresAndYear.size());

    }
}