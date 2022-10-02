package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class FilmServiceTest {
    @Autowired
    FilmService service;

    @Test
    void shouldAddWhenAddValidFilmData() throws Exception {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995,5,26));
        film.setDuration(100L);
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmNameEmpty() throws Exception {
        Film film = new Film();
        film.setName("");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1895,12,28));
        film.setDuration(100L);
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("Ошибка валидации Фильма: " +
                "Имя должно содержать буквенные символы. ", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmNameBlank() throws Exception {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1895,12,28));
        film.setDuration(100L);
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("Ошибка валидации Фильма: " +
                "Имя должно содержать буквенные символы. ", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmDuration() throws Exception {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1995,5,26));
        film.setDuration(-100L);
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("Ошибка валидации Фильма: " +
                "Продолжительность фильма не может быть отрицательной. ", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmReleaseDate() throws Exception {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description");
        film.setReleaseDate(LocalDate.of(1895,12,27));
        film.setDuration(100L);
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("Ошибка валидации Фильма: " +
                "Дата релиза не может быть раньше 28 Декабря 1895г.", ex.getMessage());
    }

    @Test
    void shouldAddWhenAddValidFilmReleaseDateBoundary() throws Exception {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1895,12,28));
        film.setDuration(100L);
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenAddFailedFilmDescription() throws Exception {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Failed description. Failed description. Failed description. Failed description. " +
                "Failed description. Failed description. Failed description. Failed description. " +
                "Failed description. Failed description. F");
        film.setReleaseDate(LocalDate.of(1995,5,26));
        film.setDuration(100L);
        FilmValidationException ex = assertThrows(FilmValidationException.class, () -> service.add(film));
        assertEquals("Ошибка валидации Фильма: " +
                "Описание фильма не должно превышать 200 символов. ", ex.getMessage());
    }

    @Test
    void shouldAddWhenAddFilmDescriptionBoundary() throws Exception {
        Film film = new Film();
        film.setName("Correct Name");
        film.setDescription("Correct description. Correct description. Correct description. Correct description. " +
                "Correct description. Correct description. Correct description. Correct description. " +
                "Correct description. Correct des");
        film.setReleaseDate(LocalDate.of(1995,5,26));
        film.setDuration(100L);
        Film addedFilm = service.add(film);
        assertNotEquals(0, addedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenUpdateFailedFilmId() throws Exception {
        Film film = new Film();
        film.setId(999);
        film.setName("Correct Name");
        film.setDescription("Correct description.");
        film.setReleaseDate(LocalDate.of(1995,5,26));
        film.setDuration(100L);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(film));
        assertEquals("Фильм с идентификатором 999 не зарегистрирован!", ex.getMessage());
    }
}