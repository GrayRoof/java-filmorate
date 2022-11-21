package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DBFilmStorageTest {

    private final DBFilmStorage filmStorage;

    @Test
    public void testGetFilmById() {

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
    void getAllFilms() {
        Film first = new Film(0,
                "first",
                "first description",
                LocalDate.now().minusYears(8),
                90L,
                3,
                new Mpa(1,"o","o"),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new ArrayList<>());
        Film second = new Film(0,
                "second",
                "second description",
                LocalDate.now().minusYears(15),
                100L,
                2,
                new Mpa(3,"o","o"),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new ArrayList<>());
        filmStorage.addFilm(first);
        filmStorage.addFilm(second);

        Collection<Film> dbFilms = filmStorage.getAllFilms();
        assertEquals(2, dbFilms.size());
    }

    @Test
    void updateFilm() {
        Film first = new Film(0,
                "first",
                "first description",
                LocalDate.now().minusYears(8),
                90L,
                3,
                new Mpa(1,"o","o"),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new ArrayList<>());
        Film added = filmStorage.addFilm(first);
        added.setName("update");
        filmStorage.updateFilm(added);
        Film dbFilm = filmStorage.getFilm(added.getId());
        assertThat(dbFilm).hasFieldOrPropertyWithValue("name", "update");
    }

    @Test
    void deleteFilm() {
        Film first = new Film(0,
                "first",
                "first description",
                LocalDate.now().minusYears(8),
                90L,
                3,
                new Mpa(1,"o","o"),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new ArrayList<>());
        Film second = new Film(0,
                "second",
                "second description",
                LocalDate.now().minusYears(15),
                100L,
                2,
                new Mpa(3,"o","o"),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new ArrayList<>());
        Film addedFirst = filmStorage.addFilm(first);
        Film addedSecond = filmStorage.addFilm(second);

        Collection<Film> beforeDelete = filmStorage.getAllFilms();
        filmStorage.deleteFilm(addedFirst);
        Collection<Film> afterDelete = filmStorage.getAllFilms();
        assertEquals(beforeDelete.size() - 1, afterDelete.size());
    }
}