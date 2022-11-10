package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DBFilmStorageTest {

    private final DBFilmStorage filmStorage;

    @Test
    public void testFindFilmById() {

        Film createFilm = new Film();
        createFilm.setMpa(new Mpa(1,"mpa", "desc"));
        createFilm.setName("new film");
        createFilm.setDescription("desc");
        createFilm.setReleaseDate(LocalDate.now().minusYears(10));
        createFilm.setDuration(60);
        createFilm.setRate(5);
        filmStorage.addFilm(createFilm);

        Film dbFilm = filmStorage.getFilm(1);
        assertThat(dbFilm).hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    void getFilm() {
    }

    @Test
    void getAllFilms() {
    }

    @Test
    void addFilm() {
    }

    @Test
    void updateFilm() {
    }

    @Test
    void deleteFilm() {
    }

    @Test
    void addLike() {
    }

    @Test
    void deleteLike() {
    }

    @Test
    void getMostPopularFilms() {
    }
}